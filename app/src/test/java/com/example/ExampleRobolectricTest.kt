package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.PORTAL_HOME_URL
import com.example.ui.PortalSheet
import com.example.ui.PortalViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pora Lekha Hobe", appName)
  }

  @Test
  fun `portal view model initializes with target website and handles fullscreen and sheets`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = PortalViewModel(app)

    assertEquals(PORTAL_HOME_URL, viewModel.uiState.value.currentUrl)
    assertFalse(viewModel.uiState.value.isFullscreen)
    assertNull(viewModel.uiState.value.activeSheet)

    viewModel.toggleFullscreen()
    assertTrue(viewModel.uiState.value.isFullscreen)
    viewModel.exitFullscreen()
    assertFalse(viewModel.uiState.value.isFullscreen)

    viewModel.openSheet(PortalSheet.ABOUT)
    assertEquals(PortalSheet.ABOUT, viewModel.uiState.value.activeSheet)
    viewModel.dismissSheet()
    assertNull(viewModel.uiState.value.activeSheet)
  }
}
