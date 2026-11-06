package com.mp3max.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import android.content.ComponentName
import androidx.media3.common.C
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import java.util.concurrent.Executor
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val Background = Color(0xFF090B10)
private val Panel = Color(0xFF171B25)
private val Accent = Color(0xFF35D6B0)

data class AudioResult(
    val title: String,
    val url: String,
    val licence: String,
    val source: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Background,
                    surface = Panel,
                    primary = Accent
                )
            ) {
                MP3MaxScreen()
            }
        }
    }
}

@Composable
fun MP3MaxScreen() {
    val context = LocalContext.current

    var quality by remember { mutableStateOf("320 kbps") }
    var audioUrl by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<AudioResult>()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("No audio selected") }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedFile = uri
            fileName = uri.lastPathSegment ?: "Audio selected"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MP3", color = Color.White,
                        fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("MAX", color = Accent,
                        fontSize = 30.sp, fontWeight = FontWeight.Black)
                }
                Icon(Icons.Default.GraphicEq, null,
                    tint = Accent, modifier = Modifier.size(45.dp))
            }

            Spacer(Modifier.height(22.dp))

            Text("Your Music. Your Quality.",
                color = Color.White, fontSize = 23.sp,
                fontWeight = FontWeight.Bold)

            Text("Your personal audio studio",
                color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(22.dp))

            // MUSIC CATALOGUE SEARCH

            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null,
                            tint = Accent, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("SEARCH FREE AUDIO",
                            color = Color.White, fontSize = 19.sp,
                            fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Search openly licensed audio on Wikimedia Commons.",
                        color = Color.Gray, fontSize = 13.sp)

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Song, sound or artist") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.MusicNote, null) }
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (searchText.isBlank()) {
                                message = "Enter something to search"
                            } else {
                                loading = true
                                message = ""
                                results = emptyList()
                                searchCommons(searchText) { found, error ->
                                    results = found
                                    loading = false
                                    message = error
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent
                        )
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("SEARCH AUDIO",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold)
                    }

                    if (loading) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Accent
                        )
                    }

                    if (message.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(message, color = Accent, fontSize = 12.sp)
                    }

                    results.forEach { item ->
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Background
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(item.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold)

                                Spacer(Modifier.height(5.dp))

                                Text("Licence: ${item.licence}",
                                    color = Accent, fontSize = 12.sp)

                                Text("Check the source page for reuse conditions.",
                                    color = Color.Gray, fontSize = 11.sp)

                                Spacer(Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        downloadAudio(context, item.url)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Accent
                                    )
                                ) {
                                    Icon(Icons.Default.Download, null,
                                        tint = Color.Black)
                                    Spacer(Modifier.width(6.dp))
                                    Text("DOWNLOAD",
                                        color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // DIRECT AUDIO DOWNLOAD

            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("DIRECT AUDIO DOWNLOADER",
                        color = Color.White, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = audioUrl,
                        onValueChange = { audioUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Paste direct audio URL") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Link, null) }
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { downloadAudio(context, audioUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent
                        )
                    ) {
                        Icon(Icons.Default.Download, null,
                            tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("DOWNLOAD AUDIO", color = Color.Black)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // CONVERTER INTERFACE

            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Icon(Icons.Default.AudioFile, null,
                        tint = Accent, modifier = Modifier.size(35.dp))

                    Spacer(Modifier.height(8.dp))

                    Text("Audio Converter",
                        color = Color.White, fontSize = 20.sp,
                        fontWeight = FontWeight.Bold)

                    Text("Select an audio file and choose quality.",
                        color = Color.Gray, fontSize = 13.sp)

                    Spacer(Modifier.height(15.dp))

                    Text("OUTPUT QUALITY",
                        color = Accent, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("128 kbps", "192 kbps", "256 kbps", "320 kbps")
                            .forEach { option ->
                                FilterChip(
                                    selected = quality == option,
                                    onClick = { quality = option },
                                    label = { Text(option, fontSize = 10.sp) }
                                )
                            }
                    }

                    Button(
                        onClick = { picker.launch(arrayOf("audio/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent
                        )
                    ) {
                        Icon(Icons.Default.FolderOpen, null,
                            tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("SELECT AUDIO FILE", color = Color.Black)
                    }

                    Text(fileName, color = Color.White,
                        fontSize = 12.sp)
                    Text("Selected quality: $quality",
                        color = Accent, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            AudioPlayerCard(selectedFile = selectedFile)
            Spacer(Modifier.height(20.dp))
            Text("MP3 MAX • AUDIO STUDIO",
                color = Color.Gray, fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

private fun searchCommons(
    query: String,
    onResult: (List<AudioResult>, String) -> Unit
) {
    Thread {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val api = URL(
                "https://commons.wikimedia.org/w/api.php" +
                "?action=query&generator=search" +
                "&gsrsearch=$encoded%20filemime%3Aaudio" +
                "&gsrnamespace=6&gsrlimit=12" +
                "&prop=imageinfo&iiprop=url%7Cextmetadata&format=json"
            )

            val connection = api.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty(
                "User-Agent", "MP3MAX/0.1 Android audio search"
            )

            val response = connection.inputStream.bufferedReader().use {
                it.readText()
            }
            connection.disconnect()

            val root = JSONObject(response)
            val pages = root.optJSONObject("query")
                ?.optJSONObject("pages")

            val found = mutableListOf<AudioResult>()

            if (pages != null) {
                val keys = pages.keys()
                while (keys.hasNext()) {
                    val page = pages.getJSONObject(keys.next())
                    val info = page.optJSONArray("imageinfo")
                        ?.optJSONObject(0) ?: continue

                    val fileUrl = info.optString("url")
                    if (!fileUrl.startsWith("https://")) continue

                    val metadata = info.optJSONObject("extmetadata")
                    val licence = metadata
                        ?.optJSONObject("LicenseShortName")
                        ?.optString("value")
                        ?: "Check source page"

                    val source = info.optString("descriptionurl")

                    found.add(
                        AudioResult(
                            title = page.optString("title")
                                .removePrefix("File:"),
                            url = fileUrl,
                            licence = licence.replace(
                                Regex("<[^>]*>"), ""
                            ),
                            source = source
                        )
                    )
                }
            }

            Handler(Looper.getMainLooper()).post {
                onResult(
                    found,
                    if (found.isEmpty()) "No audio results found." else ""
                )
            }

        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                onResult(
                    emptyList(),
                    "Search failed. Check your internet connection."
                )
            }
        }
    }.start()
}

private fun downloadAudio(context: Context, input: String) {
    try {
        val uri = Uri.parse(input.trim())

        if (
            (uri.scheme != "https" && uri.scheme != "http") ||
            uri.host.isNullOrBlank()
        ) {
            Toast.makeText(context, "Invalid audio URL",
                Toast.LENGTH_LONG).show()
            return
        }

        val name = URLUtil.guessFileName(
            input.trim(), null, "audio"
        ).replace(Regex("[^A-Za-z0-9._-]"), "_")

        val request = DownloadManager.Request(uri)
            .setTitle(name)
            .setDescription("MP3 MAX audio download")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, name
            )

        val manager = context.getSystemService(
            Context.DOWNLOAD_SERVICE
        ) as DownloadManager

        manager.enqueue(request)

        Toast.makeText(context, "Download started!",
            Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}",
            Toast.LENGTH_LONG).show()
    }
}




@Composable
fun AudioPlayerCard(selectedFile: Uri?) {
    val context = LocalContext.current

    var controller by remember {
        mutableStateOf<MediaController?>(null)
    }
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0L) }
    var currentPosition by remember { mutableStateOf(0L) }
    var loaded by remember { mutableStateOf(false) }
    var playlistUris by remember { mutableStateOf(emptyList<Uri>()) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }


    val playlistPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
            }

            playlistUris = uris

            val items = uris.map { uri ->
                MediaItem.fromUri(uri)
            }

            controller?.let { player ->
                player.setMediaItems(items)
                player.prepare()
                player.play()
                loaded = true
            }
        }
    }

    DisposableEffect(context) {
        val token = SessionToken(
            context,
            ComponentName(context, MP3MaxPlaybackService::class.java)
        )

        val future = MediaController.Builder(context, token).buildAsync()

        future.addListener(
            {
                try {
                    controller = future.get()
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "Player connection failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            Executor { command -> command.run() }
        )

        onDispose {
            MediaController.releaseFuture(future)
        }
    }

    LaunchedEffect(controller, selectedFile, playlistUris) {
        val player = controller
        if (player != null && selectedFile != null && playlistUris.isEmpty()) {
            player.setMediaItem(MediaItem.fromUri(selectedFile))
            player.prepare()
            loaded = true
        }
    }

    LaunchedEffect(controller) {
        while (true) {
            val player = controller
            if (player != null) {
                playing = player.isPlaying
                duration = player.duration.takeIf { it != C.TIME_UNSET }
                    ?.coerceAtLeast(0L) ?: 0L
                currentPosition = player.currentPosition.coerceAtLeast(0L)

                if (duration > 0L) {
                    progress = currentPosition.toFloat() / duration.toFloat()
                }
            }
            delay(500)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "MP3 MAX PLAYER",
                color = Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    playlistPicker.launch(arrayOf("audio/*"))
                },
                enabled = controller != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent
                )
            ) {
                Icon(
                    Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(Modifier.width(8.dp))
                Text("SELECT PLAYLIST FILES", color = Color.Black)
            }

            Text(
                "${playlistUris.size} tracks in playlist",
                color = Accent,
                fontSize = 13.sp
            )

            playlistUris.take(8).forEachIndexed { index, uri ->
                Text(
                    "${index + 1}. ${uri.lastPathSegment ?: "Audio track"}",
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                if (loaded) "Audio ready to play"
                else "Select an audio file or playlist",
                color = Color.White,
                fontSize = 13.sp
            )

            Slider(
                value = progress.coerceIn(0f, 1f),
                onValueChange = { progress = it },
                onValueChangeFinished = {
                    if (duration > 0L) {
                        controller?.seekTo((duration * progress).toLong())
                    }
                },
                enabled = loaded && duration > 0L
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatPlayerTime(currentPosition), color = Color.Gray)
                Text(formatPlayerTime(duration), color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { controller?.seekToPreviousMediaItem() },
                    enabled = loaded
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Accent
                    )
                }

                IconButton(
                    onClick = {
                        controller?.let {
                            if (it.isPlaying) it.pause() else it.play()
                        }
                    },
                    enabled = loaded
                ) {
                    Icon(
                        if (playing) Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = "Play or pause",
                        tint = Accent,
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(
                    onClick = { controller?.seekToNextMediaItem() },
                    enabled = loaded
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Accent
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        shuffleEnabled = !shuffleEnabled
                        controller?.shuffleModeEnabled = shuffleEnabled
                    },
                    enabled = loaded
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleEnabled) Accent else Color.Gray
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "SHUFFLE",
                        color = if (shuffleEnabled) Accent else Color.Gray
                    )
                }

                TextButton(
                    onClick = {
                        repeatMode = when (repeatMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                        controller?.repeatMode = repeatMode
                    },
                    enabled = loaded
                ) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode == Player.REPEAT_MODE_OFF)
                            Color.Gray else Accent
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        when (repeatMode) {
                            Player.REPEAT_MODE_ALL -> "REPEAT ALL"
                            Player.REPEAT_MODE_ONE -> "REPEAT ONE"
                            else -> "REPEAT OFF"
                        },
                        color = if (repeatMode == Player.REPEAT_MODE_OFF)
                            Color.Gray else Accent
                    )
                }
            }
        }
    }
}

private fun formatPlayerTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
