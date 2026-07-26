package com.example

import android.os.Bundle
import android.view.WindowManager
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainFarmersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FarmersViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    try {
      super.onCreate(savedInstanceState)
    } catch (e: Exception) {
      // Fallback or ignore startup system-level initialization exceptions
    }

    try {
      enableEdgeToEdge()
    } catch (e: Exception) {
      // Avoid crash if system window insets service throws exceptions
    }

    try {
      setContent {
        MyApplicationTheme {
          Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            val viewModel: FarmersViewModel = viewModel()
            MainFarmersScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          }
        }
      }
    } catch (e: Exception) {
      // Avoid crash in composition
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    try {
      super.onWindowFocusChanged(hasFocus)
    } catch (e: Exception) {
      // Catch DeadObjectException or improper window visibility dispatching issues
    }
  }

  override fun onAttachedToWindow() {
    try {
      super.onAttachedToWindow()
    } catch (e: Exception) {
      // Catch system window attachment exceptions
    }
  }

  override fun onDetachedFromWindow() {
    try {
      super.onDetachedFromWindow()
    } catch (e: Exception) {
      // Catch system window detachment exceptions
    }
  }

  override fun onWindowAttributesChanged(params: WindowManager.LayoutParams?) {
    try {
      super.onWindowAttributesChanged(params)
    } catch (e: Exception) {
      // Catch layout params propagation exceptions
    }
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    return try {
      super.dispatchTouchEvent(ev)
    } catch (e: Exception) {
      true // consume touch event and prevent crash if window/view is dead
    }
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    return try {
      super.dispatchKeyEvent(event)
    } catch (e: Exception) {
      true // consume key event and prevent crash if window/view is dead
    }
  }
}
