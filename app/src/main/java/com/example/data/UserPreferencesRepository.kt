package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "portal_prefs")

data class UserPreferences(
  val textZoomPercent: Int = 100,
  val desktopMode: Boolean = false,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

class UserPreferencesRepository(private val context: Context) {

  private object Keys {
    val TEXT_ZOOM_PERCENT = intPreferencesKey("text_zoom_percent")
    val DESKTOP_MODE = booleanPreferencesKey("desktop_mode")
    val THEME_MODE = stringPreferencesKey("theme_mode")
  }

  val preferences: Flow<UserPreferences> =
    context.dataStore.data.map { prefs ->
      val zoom = (prefs[Keys.TEXT_ZOOM_PERCENT] ?: 100).coerceIn(80, 150)
      val desktop = prefs[Keys.DESKTOP_MODE] ?: false
      val theme =
        prefs[Keys.THEME_MODE]?.let { raw ->
          runCatching { ThemeMode.valueOf(raw) }.getOrNull()
        } ?: ThemeMode.SYSTEM
      UserPreferences(
        textZoomPercent = zoom,
        desktopMode = desktop,
        themeMode = theme,
      )
    }

  suspend fun setTextZoomPercent(percent: Int) {
    context.dataStore.edit { prefs ->
      prefs[Keys.TEXT_ZOOM_PERCENT] = percent.coerceIn(80, 150)
    }
  }

  suspend fun setDesktopMode(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.DESKTOP_MODE] = enabled
    }
  }

  suspend fun setThemeMode(mode: ThemeMode) {
    context.dataStore.edit { prefs ->
      prefs[Keys.THEME_MODE] = mode.name
    }
  }
}
