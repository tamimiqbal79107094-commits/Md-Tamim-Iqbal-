package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UserPreferences
import com.example.data.UserPreferencesRepository
import com.example.ui.theme.ThemeMode
import com.example.util.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val PORTAL_HOME_URL = "http://poralekhahobe.page.gd/index.html"

data class WebErrorInfo(
  val errorCode: Int,
  val description: String,
  val failingUrl: String,
)

enum class PortalSheet {
  SETTINGS,
  ABOUT,
}

sealed interface WebCommand {
  data object GoBack : WebCommand
  data object GoForward : WebCommand
  data object Reload : WebCommand
  data class LoadUrl(val url: String) : WebCommand
}

data class PortalUiState(
  val currentUrl: String = PORTAL_HOME_URL,
  val pageTitle: String = "Pora Lekha Hobe",
  val loadProgress: Int = 0,
  val isLoading: Boolean = true,
  val canGoBack: Boolean = false,
  val canGoForward: Boolean = false,
  val errorInfo: WebErrorInfo? = null,
  val showSplash: Boolean = true,
  val isFullscreen: Boolean = false,
  val isOnline: Boolean = true,
  val preferences: UserPreferences = UserPreferences(),
  val activeSheet: PortalSheet? = null,
)

class PortalViewModel(application: Application) : AndroidViewModel(application) {

  private val preferencesRepository = UserPreferencesRepository(application)
  private val networkMonitor = NetworkMonitor(application)

  private val _uiState =
    MutableStateFlow(
      PortalUiState(isOnline = networkMonitor.isOnline.value)
    )
  val uiState: StateFlow<PortalUiState> = _uiState.asStateFlow()

  private val _commands = MutableSharedFlow<WebCommand>(extraBufferCapacity = 8)
  val commands: SharedFlow<WebCommand> = _commands.asSharedFlow()

  init {
    viewModelScope.launch {
      preferencesRepository.preferences.collect { prefs ->
        _uiState.update { it.copy(preferences = prefs) }
      }
    }

    viewModelScope.launch {
      var wasOffline = !networkMonitor.isOnline.value
      networkMonitor.isOnline.collect { online ->
        _uiState.update { it.copy(isOnline = online) }
        if (online && wasOffline && _uiState.value.errorInfo != null) {
          retryCurrentPage()
        }
        wasOffline = !online
      }
    }

    // Ensure branded splash always clears smoothly even on slow connections
    viewModelScope.launch {
      delay(1400L)
      _uiState.update { it.copy(showSplash = false) }
    }
  }

  override fun onCleared() {
    super.onCleared()
    networkMonitor.unregister()
  }

  fun onPageStarted(url: String?, canGoBack: Boolean, canGoForward: Boolean) {
    _uiState.update {
      it.copy(
        currentUrl = url ?: it.currentUrl,
        isLoading = true,
        errorInfo = null,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
      )
    }
  }

  fun onProgressChanged(progress: Int) {
    _uiState.update { state ->
      val finished = progress >= 100
      state.copy(
        loadProgress = progress,
        isLoading = !finished,
        showSplash = if (progress >= 85) false else state.showSplash,
      )
    }
  }

  fun onReceivedTitle(title: String?) {
    val cleaned = title?.trim().orEmpty()
    if (cleaned.isNotEmpty() && !cleaned.startsWith("http", ignoreCase = true)) {
      _uiState.update { it.copy(pageTitle = cleaned) }
    }
  }

  fun onPageFinished(url: String?, canGoBack: Boolean, canGoForward: Boolean) {
    _uiState.update {
      it.copy(
        currentUrl = url ?: it.currentUrl,
        isLoading = false,
        loadProgress = 100,
        showSplash = false,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
      )
    }
  }

  fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
    _uiState.update { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
  }

  fun onMainFrameError(errorCode: Int, description: String, failingUrl: String) {
    _uiState.update {
      it.copy(
        isLoading = false,
        showSplash = false,
        errorInfo =
          WebErrorInfo(
            errorCode = errorCode,
            description = description,
            failingUrl = failingUrl,
          ),
      )
    }
  }

  fun goBack() {
    _commands.tryEmit(WebCommand.GoBack)
  }

  fun goForward() {
    _commands.tryEmit(WebCommand.GoForward)
  }

  fun reload() {
    _uiState.update { it.copy(errorInfo = null, isLoading = true) }
    _commands.tryEmit(WebCommand.Reload)
  }

  fun loadHome() {
    _uiState.update { it.copy(errorInfo = null, isLoading = true) }
    _commands.tryEmit(WebCommand.LoadUrl(PORTAL_HOME_URL))
  }

  fun retryCurrentPage() {
    val targetUrl = _uiState.value.errorInfo?.failingUrl ?: _uiState.value.currentUrl
    _uiState.update { it.copy(errorInfo = null, isLoading = true) }
    _commands.tryEmit(WebCommand.LoadUrl(targetUrl))
  }

  fun toggleFullscreen() {
    _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
  }

  fun exitFullscreen() {
    _uiState.update { it.copy(isFullscreen = false) }
  }

  fun openSheet(sheet: PortalSheet) {
    _uiState.update { it.copy(activeSheet = sheet) }
  }

  fun dismissSheet() {
    _uiState.update { it.copy(activeSheet = null) }
  }

  fun setTextZoomPercent(percent: Int) {
    viewModelScope.launch { preferencesRepository.setTextZoomPercent(percent) }
  }

  fun setDesktopMode(enabled: Boolean) {
    viewModelScope.launch {
      preferencesRepository.setDesktopMode(enabled)
      _commands.tryEmit(WebCommand.Reload)
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
  }
}
