package com.example.prtv.ui.screen.channelbrowser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.colorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prtv.R
import com.example.prtv.ui.theme.PowerRangersTheme
import com.example.prtv.viewmodel.ChannelBrowserViewModel
import androidx.navigation.compose.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun ChannelBrowserScreen(
    navController: NavController,
    viewModel: ChannelBrowserViewModel = hiltViewModel()
) {
    var handleInput by remember { mutableStateOf("") }
    var apiKeyInput by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val channelResolved by viewModel.channelResolved.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo / Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Power Rangers Logo
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Power Rangers Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(120.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(16.dp))

            Text(
                text = "Power Rangers TV",
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.White
            )

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(8.dp))

            Text(
                text = "Enter your YouTube channel handle and API key",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.TextAlign.Center
            )
        }

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(32.dp))

        // Input Fields Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp),
            elevation = androidx.compose.foundation.layout.CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Channel Handle Field
                TextField(
                    value = handleInput,
                    onValueChange = { handleInput = it },
                    label = { Text("YouTube Channel Handle") },
                    placeholder = { Text("@PowerRangers") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedLabelColor = Color(0xFFFF5200),
                        focusedIndicatorColor = Color(0xFFFF5200),
                        cursorColor = Color(0xFFFF5200)
                    )
                )

                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(16.dp))

                // API Key Field
                TextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("YouTube Data API v3 Key") },
                    placeholder = { Text("Enter your API key (optional - uses embedded)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedLabelColor = Color(0xFFFF5200),
                        focusedIndicatorColor = Color(0xFFFF5200),
                        cursorColor = Color(0xFFFF5200)
                    )
                )

                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(24.dp))

                // Error Message
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.TextAlign.Center
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(16.dp))
                }

                // Load Channel Button
                Button(
                    onClick = {
                        viewModel.loadChannel(handleInput.trim(), apiKeyInput.trim())
                    },
                    enabled = !isLoading && handleInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5200),
                        contentColor = Color.White
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Load Channel",
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(16.dp))

        // Channel Resolved - Navigate to Playlist Browser
        if (channelResolved && !isLoading) {
            androidx.compose.runtime.LaunchedEffect(key1 = channelResolved) {
                navController.navigate("playlist_browser") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
}
