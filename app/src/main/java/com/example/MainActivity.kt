package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.example.backend.service.NotificationHelper
import com.example.frontend.MainViewModel
import com.example.frontend.navigation.NutriPathNav
import com.example.frontend.theme.NutriPathTheme

import android.view.MotionEvent
import com.example.frontend.util.KeyboardDismissManager
import com.example.frontend.util.dismissKeyboardOnTap

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null && ev.action == MotionEvent.ACTION_DOWN) {
            val focusedBounds = KeyboardDismissManager.activeFocusedBoundsInWindow
            val isImeVisible = KeyboardDismissManager.isKeyboardVisible(this)

            if (focusedBounds != null || isImeVisible) {
                val x = ev.rawX.toInt()
                val y = ev.rawY.toInt()
                val isInsideFocusedField = focusedBounds != null && focusedBounds.contains(x, y)

                if (!isInsideFocusedField) {
                    KeyboardDismissManager.dismissKeyboard(
                        context = this,
                        windowToken = currentFocus?.windowToken ?: window.decorView.windowToken
                    )
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            NutriPathTheme {
                // Request Notification permission on Android 13+ (API 33+)
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* Handled */ }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
                val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

                androidx.compose.runtime.DisposableEffect(focusManager, keyboardController) {
                    KeyboardDismissManager.globalFocusManager = focusManager
                    KeyboardDismissManager.globalKeyboardController = keyboardController
                    onDispose {
                        if (KeyboardDismissManager.globalFocusManager == focusManager) {
                            KeyboardDismissManager.globalFocusManager = null
                        }
                    }
                }

                CompositionLocalProvider(com.example.frontend.util.LocalAppLanguage provides currentLanguage) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .dismissKeyboardOnTap(focusManager, keyboardController)
                    ) {
                        NutriPathNav(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val deepLink = intent.getStringExtra(NotificationHelper.EXTRA_DEEP_LINK)
        val logId = intent.getLongExtra(NotificationHelper.EXTRA_LOG_ID, -1L)
        if (deepLink != null) {
            val resolvedId = if (logId != -1L) logId else null
            viewModel.handleDeepLink(deepLink, resolvedId)
        }
    }
}
