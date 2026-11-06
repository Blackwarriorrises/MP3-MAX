package com.mp3max.app

import android.content.Context
import android.net.Uri
import com.naman14.androidlame.LameBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

object WavMp3Converter {

    private fun readU16(file: RandomAccessFile): Int {
        val a = file.read()
        val b = file.read()
        if (a < 0 || b < 0) error("Invalid WAV header")
        return a or (b shl 8)
    }

    private fun readU32(file: RandomAccessFile): Long {
        val a = file.read()
        val b = file.read()
        val c = file.read()
        val d = file.read()
        if (a < 0 || b < 0 || c < 0 || d < 0) error("Invalid WAV header")
        return (a.toLong()) or
            (b.toLong() shl 8) or
            (c.toLong() shl 16) or
            (d.toLong() shl 24)
    }

    fun convert(
        context: Context,
        input: Uri,
        output: Uri,
        bitrate: Int
    ) {
        val temp = File.createTempFile("mp3max_", ".wav", context.cacheDir)

        try {
            context.contentResolver.openInputStream(input)?.use { source ->
                FileOutputStream(temp).use { destination ->
                    source.copyTo(destination)
                }
            } ?: error("Cannot open input audio")

            RandomAccessFile(temp, "r").use { wav ->

                val riff = ByteArray(4)
                wav.readFully(riff)
                if (String(riff) != "RIFF") error("Not a WAV file")

                readU32(wav)

                val wave = ByteArray(4)
                wav.readFully(wave)
                if (String(wave) != "WAVE") error("Invalid WAV file")

                var format = 0
                var channels = 0
                var sampleRate = 0
                var bits = 0
                var dataOffset = -1L
                var dataSize = 0L

                while (wav.filePointer + 8 <= wav.length()) {
                    val id = ByteArray(4)
                    wav.readFully(id)
                    val size = readU32(wav)
                    val next = wav.filePointer + size + (size % 2)

                    when (String(id)) {
                        "fmt " -> {
                            format = readU16(wav)
                            channels = readU16(wav)
                            sampleRate = readU32(wav).toInt()
                            readU32(wav)
                            readU16(wav)
                            bits = readU16(wav)
                        }
                        "data" -> {
                            dataOffset = wav.filePointer
                            dataSize = size
                        }
                    }

                    if (next > wav.length()) break
                    wav.seek(next)
                    if (format != 0 && dataOffset >= 0) break
                }

                if (format != 1 || bits != 16) {
                    error("Only 16-bit PCM WAV files are supported")
                }

                if (channels !in 1..2 || sampleRate <= 0 || dataOffset < 0) {
                    error("Unsupported WAV audio format")
                }

                val encoder = LameBuilder()
                    .setInSampleRate(sampleRate)
                    .setOutChannels(channels)
                    .setOutBitrate(bitrate)
                    .setOutSampleRate(sampleRate)
                    .build()

                try {
                    context.contentResolver.openOutputStream(output)?.use { out ->

                        wav.seek(dataOffset)

                        val bytes = ByteArray(8192)
                        val mp3 = ByteArray(16384)
                        var remaining = dataSize

                        while (remaining >= channels * 2) {
                            val wanted = minOf(bytes.size.toLong(), remaining).toInt()
                            val count = wav.read(bytes, 0, wanted)
                            if (count <= 0) break

                            val usable = count - (count % (channels * 2))
                            if (usable == 0) break

                            val samples = ShortArray(usable / 2)

                            for (i in samples.indices) {
                                val low = bytes[i * 2].toInt() and 0xff
                                val high = bytes[i * 2 + 1].toInt()
                                samples[i] = ((high shl 8) or low).toShort()
                            }

                            val sampleCount = samples.size / channels

                            val encoded = if (channels == 1) {
                                encoder.encode(
                                    samples, samples, sampleCount, mp3
                                )
                            } else {
                                encoder.encodeBufferInterLeaved(
                                    samples, sampleCount, mp3
                                )
                            }

                            if (encoded > 0) out.write(mp3, 0, encoded)
                            remaining -= usable
                        }

                        val finalBytes = encoder.flush(mp3)
                        if (finalBytes > 0) {
                            out.write(mp3, 0, finalBytes)
                        }
                    } ?: error("Cannot create MP3 output")

                } finally {
                    encoder.close()
                }
            }
        } finally {
            temp.delete()
        }
    }
}
