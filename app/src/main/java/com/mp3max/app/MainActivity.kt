package com.mp3max.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import android.webkit.URLUtil

private val Background = Color(0xFF090B10)
private val Panel = Color(0xFF171B25)
private val Accent = Color(0xFF35D6B0)

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
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("No audio selected") }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
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
                .padding(22.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "MP3",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "MAX",
                        color = Accent,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Icon(
                    Icons.Default.GraphicEq,
                    null,
                    tint = Accent,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(Modifier.height(25.dp))

            Text(
                "Your Music. Your Quality.",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Your personal audio studio",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(25.dp))

            // MUSIC SEARCH AND DOWNLOAD

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Panel
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = Accent,
                            modifier = Modifier.size(30.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            "MUSIC DOWNLOADER",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Download audio from authorized direct links.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = audioUrl,
                        onValueChange = { audioUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Paste audio URL") },
                        placeholder = {
                            Text("https://example.com/audio.mp3")
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Link, null)
                        }
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            downloadAudio(context, audioUrl)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent
                        )
                    ) {
                        Icon(
                            Icons.Default.Download,
                            null,
                            tint = Color.Black
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "DOWNLOAD AUDIO",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Supports direct downloadable audio links. " +
                        "Protected or restricted content is not supported.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // AUDIO CONVERTER

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Panel
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {

                    Icon(
                        Icons.Default.AudioFile,
                        null,
                        tint = Accent,
                        modifier = Modifier.size(35.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Audio Converter",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Select an audio file and choose output quality.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        "OUTPUT QUALITY",
                        color = Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf(
                            "128 kbps",
                            "192 kbps",
                            "256 kbps",
                            "320 kbps"
                        ).forEach { option ->

                            FilterChip(
                                selected = quality == option,
                                onClick = { quality = option },
                                label = {
                                    Text(option, fontSize = 10.sp)
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            audioPicker.launch(arrayOf("audio/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent
                        )
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            null,
                            tint = Color.Black
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "SELECT AUDIO FILE",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        fileName,
                        color = Color.White,
                        fontSize = 13.sp
                    )

                    Text(
                        "Selected quality: $quality",
                        color = Accent,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // MUSIC LIBRARY

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Panel
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LibraryMusic,
                        null,
                        tint = Accent,
                        modifier = Modifier.size(35.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            if (selectedFile == null)
                                "No music imported yet"
                            else
                                "Audio file selected",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            "Downloaded files are saved in Downloads.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(25.dp))

            Text(
                "MP3 MAX  •  AUDIO STUDIO",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

private fun downloadAudio(context: Context, input: String) {
    try {
        val url = input.trim()
        val uri = Uri.parse(url)

        if (
            (uri.scheme != "https" && uri.scheme != "http") ||
            uri.host.isNullOrBlank()
        ) {
            Toast.makeText(
                context,
                "Enter a valid audio URL",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val fileName = URLUtil.guessFileName(
            url,
            null,
            "audio"
        ).replace(Regex("[^A-Za-z0-9._-]"), "_")

        val request = DownloadManager.Request(uri)
            .setTitle(fileName)
            .setDescription("Downloading audio with MP3 MAX")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

        val manager = context.getSystemService(
            Context.DOWNLOAD_SERVICE
        ) as DownloadManager

        manager.enqueue(request)

        Toast.makeText(
            context,
            "Download started!",
            Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Download error: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}
