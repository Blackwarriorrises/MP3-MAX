package com.mp3max.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var quality by remember { mutableStateOf("320 kbps") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MP3", color = Color.White, fontSize = 30.sp,
                        fontWeight = FontWeight.Black)
                    Text("MAX", color = Accent, fontSize = 30.sp,
                        fontWeight = FontWeight.Black)
                }
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = "Music",
                    tint = Accent,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(Modifier.height(30.dp))

            Text("Your Music. Your Quality.",
                color = Color.White, fontSize = 23.sp,
                fontWeight = FontWeight.Bold)

            Text("High-quality audio experience",
                color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(25.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Icon(Icons.Default.AudioFile,
                        contentDescription = null,
                        tint = Accent, modifier = Modifier.size(35.dp))

                    Spacer(Modifier.height(12.dp))

                    Text("Audio Converter",
                        color = Color.White, fontSize = 20.sp,
                        fontWeight = FontWeight.Bold)

                    Text("Convert your permitted audio files",
                        color = Color.Gray, fontSize = 13.sp)

                    Spacer(Modifier.height(20.dp))

                    Text("OUTPUT QUALITY",
                        color = Accent, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("128 kbps", "192 kbps", "256 kbps", "320 kbps")
                            .forEach { option ->
                                FilterChip(
                                    selected = quality == option,
                                    onClick = { quality = option },
                                    label = { Text(option, fontSize = 10.sp) }
                                )
                            }
                    }

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent
                        )
                    ) {
                        Icon(Icons.Default.FolderOpen,
                            contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("SELECT AUDIO FILE",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(25.dp))

            Text("YOUR MUSIC LIBRARY",
                color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = Accent, modifier = Modifier.size(35.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("No music imported yet",
                            color = Color.White,
                            fontWeight = FontWeight.Medium)
                        Text("Your converted tracks will appear here",
                            color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            NavigationBar(containerColor = Panel) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.LibraryMusic, null) },
                    label = { Text("Library") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") }
                )
            }
        }
    }
}
