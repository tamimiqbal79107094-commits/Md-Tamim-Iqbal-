package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PortalScreen
import com.example.ui.PortalViewModel
import com.example.ui.theme.PoraLekhaHobeTheme

class MainActivity : ComponentActivity() {

  private val portalViewModel: PortalViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
    )
    setContent {
      val uiState by portalViewModel.uiState.collectAsStateWithLifecycle()
      PoraLekhaHobeTheme(themeMode = uiState.preferences.themeMode) {
        PortalScreen(viewModel = portalViewModel)
      }
    }
  }
}
