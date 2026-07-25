package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainFarmersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FarmersViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: FarmersViewModel = viewModel()
      val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
      MyApplicationTheme(darkTheme = darkMode) {
        val textScale by viewModel.textScale.collectAsStateWithLifecycle()
        val density = LocalDensity.current
        // Scale every sp in the app by the user's chosen text size (P1.8).
        CompositionLocalProvider(
          LocalDensity provides Density(
            density = density.density,
            fontScale = density.fontScale * textScale
          )
        ) {
          Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainFarmersScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          }
        }
      }
    }
  }
}
