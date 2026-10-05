package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DrakzoBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.AuthStep
import com.example.ui.viewmodel.DrakzoViewModel
import com.example.ui.viewmodel.HomeTab

class MainActivity : ComponentActivity() {

    private val viewModel: DrakzoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureMaxRefreshRate()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DrakzoBg
                ) {
                    DrakzoApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun configureMaxRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val modes = display?.supportedModes ?: windowManager.defaultDisplay.supportedModes
                val maxMode = modes.maxByOrNull { it.refreshRate }
                if (maxMode != null) {
                    val params = window.attributes
                    params.preferredDisplayModeId = maxMode.modeId
                    window.attributes = params
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val modes = windowManager.defaultDisplay.supportedModes
                val maxMode = modes.maxByOrNull { it.refreshRate }
                if (maxMode != null) {
                    val params = window.attributes
                    params.preferredDisplayModeId = maxMode.modeId
                    window.attributes = params
                }
            }
        } catch (_: Exception) {}
    }
}

@Composable
fun DrakzoApp(viewModel: DrakzoViewModel) {
    val destination by viewModel.currentDestination.collectAsState()
    val activeChat by viewModel.activeChat.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val authStep by viewModel.authStep.collectAsState()

    // Handle back button logically
    BackHandler(enabled = destination !is AppDestination.Splash) {
        when (destination) {
            is AppDestination.ChatDetail -> {
                viewModel.closeChat()
            }
            is AppDestination.Home -> {
                if (currentTab != HomeTab.CHATS) {
                    viewModel.setTab(HomeTab.CHATS)
                }
            }
            is AppDestination.Auth -> {
                if (authStep != AuthStep.LOGIN) {
                    viewModel.selectAuthStep(AuthStep.LOGIN)
                }
            }
            else -> {}
        }
    }

    AnimatedContent(
        targetState = destination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "app_destination_transition"
    ) { dest ->
        when (dest) {
            is AppDestination.Splash -> {
                SplashScreen()
            }
            is AppDestination.Auth -> {
                AuthScreen(viewModel = viewModel)
            }
            is AppDestination.Home -> {
                HomeScreen(viewModel = viewModel)
            }
            is AppDestination.ChatDetail -> {
                activeChat?.let { chat ->
                    ChatDetailScreen(chat = chat, viewModel = viewModel)
                } ?: run {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
