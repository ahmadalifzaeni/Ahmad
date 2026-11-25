package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LauncherScreen
import com.example.ui.theme.LuminaTheme
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()
    private var immersiveGestureEnabled: Boolean = true

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.refreshInstalledApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        registerPackageChanges()

        setContent {
            val brightness by viewModel.windowBrightness.collectAsStateWithLifecycle()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val hideSystemNav = uiState.preferences.immersiveGestureNav

            LaunchedEffect(brightness) {
                val attrs = window.attributes
                attrs.screenBrightness = brightness
                window.attributes = attrs
            }

            LaunchedEffect(hideSystemNav) {
                immersiveGestureEnabled = hideSystemNav
                applyNavigationBarMode(hideSystemNav)
            }

            LuminaTheme {
                LauncherScreen(viewModel = viewModel)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyNavigationBarMode(immersiveGestureEnabled)
        }
    }

    override fun onResume() {
        super.onResume()
        applyNavigationBarMode(immersiveGestureEnabled)
        viewModel.refreshDeviceStatus()
        viewModel.refreshInstalledApps()
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(packageChangeReceiver) }
        super.onDestroy()
    }

    private fun applyNavigationBarMode(hideThreeButtonNav: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (hideThreeButtonNav) {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    private fun registerPackageChanges() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageChangeReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(packageChangeReceiver, filter)
        }
    }
}
