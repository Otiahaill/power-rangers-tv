package com.example.prtv.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prtv.R
import com.example.prtv.ui.theme.PowerRangersTheme
import com.example.prtv.viewmodel.SettingsViewModel
import androidx.navigation.compose.NavController

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentApiKey by viewModel.currentApiKey.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    var apiKeyInput by remember { mutableStateOf(currentApiKey) }

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = { Text(text = "Settings", color = Color.White, fontSize = 20.sp) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFF5200),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // API Key Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "YouTube API Key", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Key,
                        contentDescription = "API Key",
                        tint = Color(0xFFFF5200)
                    )
                }

                Text(
                    text = "Enter your YouTube Data API v3 key to fetch playlists and videos.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                androidx.compose.material3.TextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key") },
                    placeholder = { Text("Enter API key...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                        focusedLabelColor = Color(0xFFFF5200),
                        focusedIndicatorColor = Color(0xFFFF5200),
                        cursorColor = Color(0xFFFF5200)
                    )
                )

                androidx.compose.material3.Button(
                    onClick = { viewModel.updateApiKey(apiKeyInput) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5200)
                    )
                ) {
                    Text("Save API Key", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                }
            }
        }

        // Theme Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Appearance", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Dark Mode", fontSize = 16.sp)
                    Switch(
                        checked = themeMode == 2,
                        onCheckedChange = { isChecked ->
                            viewModel.updateThemeMode(if (isChecked) 2 else 1)
                        },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            thumbColor = Color(0xFFFF5200),
                            trackColor = Color(0xFFFF5200).copy(alpha = 0.5f)
                        )
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Follow System", fontSize = 16.sp)
                    Switch(
                        checked = themeMode == 0,
                        onCheckedChange = { isChecked ->
                            viewModel.updateThemeMode(if (isChecked) 0 else 1)
                        },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            thumbColor = Color(0xFFFF5200),
                            trackColor = Color(0xFFFF5200).copy(alpha = 0.5f)
                        )
                    }
                )
            }
        )

        // About Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "About", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

                Text(
                    text = "Power Rangers TV v1.0.0",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Text(
                    text = "Watch Power Rangers episodes from YouTube as a simulated live TV broadcast.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(8.dp))

                Text(
                    text = "Power Rangers™ & © Hasbro. YouTube content from Power Rangers Official channel.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        )
    }
}
