package com.mp3max.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Black = Color(0xFF090909)
private val Panel = Color(0xFF171717)
private val Red = Color(0xFFFF2638)
private val Muted = Color(0xFFAAAAAA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Mp3MaxApp() }
    }
}

@Composable
fun Mp3MaxApp() {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("No audio file selected") }
    var bitrate by remember { mutableStateOf("320 kbps") }
    var outputFormat by remember { mutableStateOf("MP3") }
    var status by remember { mutableStateOf("Choose an audio file you own or are authorized to convert.") }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected audio"
            status = "File selected. Choose an output format and bitrate."
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Red, background = Black, surface = Panel,
            onBackground = Color.White, onSurface = Color.White
        )
    ) {
        Scaffold(containerColor = Black) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(Red, RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(29.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("MP3", fontSize = 25.sp, fontWeight = FontWeight.Black)
                            Text(" MAX", color = Red, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        }
                        Text("DOWNLOAD  •  CONVERT", color = Muted, fontSize = 10.sp, letterSpacing = 1.4.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Settings, "Settings", tint = Muted)
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(Panel, RoundedCornerShape(22.dp)).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("YOUR AUDIO WORKSPACE", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text("Make your audio\nwork for you.", fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
                    Text("Convert files stored on your device. No account or cloud upload required.", color = Muted, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeatureChip("320 kbps")
                        FeatureChip("Offline-ready")
                        FeatureChip("Private")
                    }
                }

                SectionTitle("AUDIO CONVERTER", Icons.Default.Sync)
                Column(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF343434), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedButton(
                        onClick = { picker.launch(arrayOf("audio/*", "video/mp4", "video/*")) },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.FolderOpen, null, tint = Red)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose audio or video file")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AudioFile, null, tint = Red)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(selectedName, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(if (selectedUri == null) "WAV, FLAC, M4A, MP3 and supported video" else "Selected on this device", color = Muted, fontSize = 11.sp)
                        }
                    }
                    HorizontalDivider(color = Color(0xFF333333))
                    Text("OUTPUT FORMAT", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MP3", "WAV", "M4A").forEach { format ->
                            FilterChip(
                                selected = outputFormat == format,
                                onClick = { outputFormat = format },
                                label = { Text(format) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Red,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    if (outputFormat == "MP3") {
                        Text("BITRATE", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("128 kbps", "192 kbps", "256 kbps", "320 kbps").forEach { rate ->
                                FilterChip(
                                    selected = bitrate == rate,
                                    onClick = { bitrate = rate },
                                    label = { Text(rate.substringBefore(' '), fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Red,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            status = if (selectedUri == null) {
                                "Select a file first."
                            } else {
                                "The project UI is ready. The MP3 encoding engine must be integrated before export can run."
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red)
                    ) {
                        Icon(Icons.Default.Sync, null)
                        Spacer(Modifier.width(8.dp))
                        Text("CONVERT AUDIO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Text(status, color = Muted, fontSize = 12.sp)
                }

                SectionTitle("AUTHORIZED DOWNLOADS", Icons.Default.Download)
                Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(16.dp)) {
                    Text("Add downloads from sources that explicitly permit saving the audio.", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("This starter does not scrape streaming services or bypass DRM. A source-specific official API or direct authorized file URL can be added in a later integration.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, null, tint = Red)
                    Spacer(Modifier.width(8.dp))
                    Text("MP3 MAX", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("Version 0.1 • Prototype", color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun FeatureChip(label: String) {
    Surface(color = Color(0xFF292020), shape = RoundedCornerShape(50)) {
        Text(label, color = Color(0xFFFFA2AA), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Red, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.2.sp)
    }
}
