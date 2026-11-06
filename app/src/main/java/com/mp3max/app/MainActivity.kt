package com.mp3max.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.naman14.androidlame.LameBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var inputUri by remember { mutableStateOf<Uri?>(null) }
            var fileName by remember { mutableStateOf("No file selected") }
            var bitrate by remember { mutableIntStateOf(192) }
            var status by remember { mutableStateOf("Choose a WAV file") }

            val picker = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    inputUri = uri
                    fileName = uri.lastPathSegment ?: "WAV file selected"
                    status = "Ready to convert"
                }
            }

            val saver = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("audio/mpeg")
            ) { outputUri ->
                val source = inputUri

                if (source != null && outputUri != null) {
                    status = "Converting..."

                    lifecycleScope.launch {
                        status = withContext(Dispatchers.IO) {
                            try {
                                val input = contentResolver.openInputStream(source)
                                    ?: error("Cannot read input file")

                                val output = contentResolver.openOutputStream(outputUri)
                                    ?: error("Cannot save output file")

                                input.use { inputStream ->
                                    output.use { outputStream ->
                                        convertWavToMp3(
                                            inputStream,
                                            outputStream,
                                            bitrate
                                        )
                                    }
                                }

                                "SUCCESS! MP3 saved."
                            } catch (e: Exception) {
                                "ERROR: ${e.message}"
                            }
                        }
                    }
                }
            }

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "MP3 MAX",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text("WAV to MP3 Converter")

                    Button(
                        onClick = {
                            picker.launch(
                                arrayOf(
                                    "audio/wav",
                                    "audio/x-wav",
                                    "audio/wave"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SELECT WAV FILE")
                    }

                    Text(fileName)

                    Text("Select MP3 bitrate")

                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf(128, 192, 256, 320).forEach { value ->
                            FilterChip(
                                selected = bitrate == value,
                                onClick = { bitrate = value },
                                label = { Text("$value") }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (inputUri == null) {
                                status = "Please select a WAV file first."
                            } else {
                                saver.launch("MP3_MAX_Converted.mp3")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CONVERT TO MP3")
                    }

                    Text(status)

                    Text(
                        "Supports uncompressed 16-bit PCM WAV files. " +
                            "Convert audio you own or have permission to use."
                    )
                }
            }
        }
    }

    private fun convertWavToMp3(
        input: InputStream,
        output: OutputStream,
        bitrate: Int
    ) {
        val header = ByteArray(12)
        readFully(input, header)

        require(
            String(header, 0, 4, Charsets.US_ASCII) == "RIFF" &&
            String(header, 8, 4, Charsets.US_ASCII) == "WAVE"
        ) {
            "Invalid WAV file"
        }

        var channels = 0
        var sampleRate = 0
        var bitsPerSample = 0
        var dataSize = -1L

        while (true) {
            val chunkHeader = ByteArray(8)
            if (!readFullyOrEnd(input, chunkHeader)) break

            val chunkName = String(
                chunkHeader,
                0,
                4,
                Charsets.US_ASCII
            )

            val chunkSize = littleInt(chunkHeader, 4).toLong() and 0xffffffffL

            when (chunkName) {
                "fmt " -> {
                    require(chunkSize >= 16 && chunkSize <= 65536) {
                        "Unsupported WAV format header"
                    }

                    val fmt = ByteArray(chunkSize.toInt())
                    readFully(input, fmt)

                    val audioFormat = littleShort(fmt, 0)
                    channels = littleShort(fmt, 2)
                    sampleRate = littleInt(fmt, 4)
                    bitsPerSample = littleShort(fmt, 14)

                    require(audioFormat == 1) {
                        "Only PCM WAV is supported"
                    }
                }

                "data" -> {
                    dataSize = chunkSize
                    break
                }

                else -> skipFully(input, chunkSize)
            }

            if ((chunkSize and 1L) != 0L) {
                skipFully(input, 1)
            }
        }

        require(channels == 1 || channels == 2) {
            "Only mono or stereo WAV is supported"
        }

        require(bitsPerSample == 16) {
            "Only 16-bit WAV is supported"
        }

        require(sampleRate in 8000..48000) {
            "Unsupported sample rate"
        }

        require(dataSize >= 0) {
            "WAV audio data not found"
        }

        val encoder = LameBuilder()
            .setInSampleRate(sampleRate)
            .setOutChannels(channels)
            .setOutBitrate(bitrate)
            .setOutSampleRate(sampleRate)
            .build()

        val inputBuffer = ByteArray(8192)
        val mp3Buffer = ByteArray(16384)
        val samplesPerChannel = inputBuffer.size / (2 * channels)

        var remaining = dataSize

        try {
            while (remaining >= 2L * channels) {
                val wanted = minOf(
                    inputBuffer.size.toLong(),
                    remaining - (remaining % (2L * channels))
                ).toInt()

                val count = readSome(input, inputBuffer, wanted)
                if (count <= 0) break

                val usable = count - (count % (2 * channels))
                if (usable == 0) break

                val frames = usable / (2 * channels)
                val left = ShortArray(frames)
                val right = ShortArray(frames)

                for (i in 0 until frames) {
                    val offset = i * channels * 2
                    left[i] = littleSample(inputBuffer, offset)

                    right[i] = if (channels == 2) {
                        littleSample(inputBuffer, offset + 2)
                    } else {
                        left[i]
                    }
                }

                val encoded = encoder.encode(
                    left,
                    right,
                    frames,
                    mp3Buffer
                )

                if (encoded > 0) {
                    output.write(mp3Buffer, 0, encoded)
                }

                remaining -= usable.toLong()
            }

            val flushed = encoder.flush(mp3Buffer)
            if (flushed > 0) {
                output.write(mp3Buffer, 0, flushed)
            }

            output.flush()
        } finally {
            encoder.close()
        }
    }

    private fun readFully(input: InputStream, buffer: ByteArray) {
        var offset = 0
        while (offset < buffer.size) {
            val count = input.read(buffer, offset, buffer.size - offset)
            if (count < 0) error("Unexpected end of WAV file")
            offset += count
        }
    }

    private fun readFullyOrEnd(
        input: InputStream,
        buffer: ByteArray
    ): Boolean {
        var offset = 0

        while (offset < buffer.size) {
            val count = input.read(buffer, offset, buffer.size - offset)

            if (count < 0) {
                if (offset == 0) return false
                error("Incomplete WAV chunk header")
            }

            offset += count
        }

        return true
    }

    private fun readSome(
        input: InputStream,
        buffer: ByteArray,
        wanted: Int
    ): Int {
        var total = 0

        while (total < wanted) {
            val count = input.read(buffer, total, wanted - total)
            if (count < 0) break
            if (count == 0) continue
            total += count
        }

        return total
    }

    private fun skipFully(input: InputStream, amount: Long) {
        var remaining = amount

        while (remaining > 0) {
            val skipped = input.skip(remaining)

            if (skipped > 0) {
                remaining -= skipped
            } else {
                if (input.read() < 0) error("Unexpected end of WAV file")
                remaining--
            }
        }
    }

    private fun littleShort(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 255) or
            ((data[offset + 1].toInt() and 255) shl 8)
    }

    private fun littleInt(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 255) or
            ((data[offset + 1].toInt() and 255) shl 8) or
            ((data[offset + 2].toInt() and 255) shl 16) or
            ((data[offset + 3].toInt() and 255) shl 24)
    }

    private fun littleSample(data: ByteArray, offset: Int): Short {
        return littleShort(data, offset).toShort()
    }
}
