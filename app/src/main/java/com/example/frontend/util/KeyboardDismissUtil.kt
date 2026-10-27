package com.example.frontend.util

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.IBinder
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.absoluteValue

/**
 * Global Keyboard & Focus dismissal manager.
 * Tracks the screen bounds of any currently focused TextField.
 * When the user taps any random/empty space outside the active TextField, or scrolls,
 * the keyboard and focus are cleanly dismissed without requiring the user to press the Back button.
 */
object KeyboardDismissManager {
    var globalFocusManager: FocusManager? = null
    var globalKeyboardController: SoftwareKeyboardController? = null
    var activeFocusedBoundsInWindow: Rect? = null
    var lastFocusedCoordinates: LayoutCoordinates? = null

    fun dismissKeyboard(context: Context? = null, windowToken: IBinder? = null) {
        try {
            globalFocusManager?.clearFocus(force = true)
            globalKeyboardController?.hide()
            if (context != null) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                val token = windowToken
                    ?: (context as? Activity)?.currentFocus?.windowToken
                    ?: (context as? Activity)?.window?.decorView?.windowToken
                if (token != null) {
                    imm?.hideSoftInputFromWindow(token, 0)
                }
            }
            activeFocusedBoundsInWindow = null
            lastFocusedCoordinates = null
        } catch (_: Exception) {}
    }

    fun updateBoundsFromCoordinates(coordinates: LayoutCoordinates) {
        if (!coordinates.isAttached) return
        try {
            val bounds = coordinates.boundsInWindow()
            // Add a 4px cushion around boundaries for natural finger taps
            activeFocusedBoundsInWindow = Rect(
                (bounds.left - 4f).toInt(),
                (bounds.top - 4f).toInt(),
                (bounds.right + 4f).toInt(),
                (bounds.bottom + 4f).toInt()
            )
        } catch (_: Exception) {
            activeFocusedBoundsInWindow = null
        }
    }

    fun onFocusChanged(isFocused: Boolean, coordinates: LayoutCoordinates?) {
        if (isFocused) {
            lastFocusedCoordinates = coordinates
            coordinates?.let { updateBoundsFromCoordinates(it) }
        } else {
            if (lastFocusedCoordinates == coordinates || lastFocusedCoordinates == null) {
                activeFocusedBoundsInWindow = null
                lastFocusedCoordinates = null
            }
        }
    }

    fun isKeyboardVisible(activity: Activity): Boolean {
        return try {
            val insets = ViewCompat.getRootWindowInsets(activity.window.decorView)
            insets?.isVisible(WindowInsetsCompat.Type.ime()) == true
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * Modifier extension that dismisses the soft keyboard and clears focus
 * whenever a user taps on any random/empty space on the screen, or scrolls.
 */
fun Modifier.dismissKeyboardOnTap(
    focusManager: FocusManager? = null,
    keyboardController: SoftwareKeyboardController? = null
): Modifier = composed {
    val effectiveFocusManager = focusManager ?: LocalFocusManager.current
    val effectiveKeyboardController = keyboardController ?: LocalSoftwareKeyboardController.current

    DisposableEffect(effectiveFocusManager, effectiveKeyboardController) {
        KeyboardDismissManager.globalFocusManager = effectiveFocusManager
        KeyboardDismissManager.globalKeyboardController = effectiveKeyboardController
        onDispose { }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If scrolling vertically or horizontally by more than 2 pixels, dismiss keyboard
                if (available.y.absoluteValue > 2f || available.x.absoluteValue > 2f) {
                    KeyboardDismissManager.dismissKeyboard()
                }
                return Offset.Zero
            }
        }
    }

    this
        .nestedScroll(nestedScrollConnection)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            KeyboardDismissManager.dismissKeyboard()
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    KeyboardDismissManager.dismissKeyboard()
                }
            )
        }
}

/**
 * Modifier for TextFields and input components that registers the active field's screen bounds.
 * When tapping outside these bounds, MainActivity and dismissKeyboardOnTap immediately dismiss the keyboard.
 */
fun Modifier.trackKeyboardFocusBounds(): Modifier = composed {
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    this
        .onGloballyPositioned { layoutCoordinates ->
            coordinates = layoutCoordinates
            if (KeyboardDismissManager.lastFocusedCoordinates == layoutCoordinates) {
                KeyboardDismissManager.updateBoundsFromCoordinates(layoutCoordinates)
            }
        }
        .onFocusChanged { focusState ->
            KeyboardDismissManager.onFocusChanged(focusState.isFocused, coordinates)
        }
}
