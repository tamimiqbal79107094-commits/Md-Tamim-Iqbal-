package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.PortalAboutSheet
import com.example.ui.components.PortalErrorView
import com.example.ui.components.PortalSettingsSheet
import com.example.ui.components.PortalWebView
import com.example.ui.components.SplashOverlay
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandNavy
import kotlinx.coroutines.launch

@Composable
fun PortalScreen(viewModel: PortalViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Handle Android back button cleanly
  BackHandler(enabled = uiState.isFullscreen || uiState.canGoBack) {
    when {
      uiState.isFullscreen -> viewModel.exitFullscreen()
      uiState.canGoBack -> viewModel.goBack()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val useRail = maxWidth >= 600.dp

      Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
          AnimatedVisibility(
            visible = !uiState.isFullscreen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            PortalTopBar(
              title = uiState.pageTitle,
              currentUrl = uiState.currentUrl,
              isOnline = uiState.isOnline,
              isLoading = uiState.isLoading,
              loadProgress = uiState.loadProgress,
              onRefresh = { viewModel.reload() },
              onOpenSettings = { viewModel.openSheet(PortalSheet.SETTINGS) },
              onOpenAbout = { viewModel.openSheet(PortalSheet.ABOUT) },
              onToggleFullscreen = { viewModel.toggleFullscreen() },
              onShare = { shareUrl(context, uiState.pageTitle, uiState.currentUrl) },
              onOpenExternal = {
                openInExternalBrowser(context, uiState.currentUrl) {
                  scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.message_no_browser))
                  }
                }
              },
            )
          }
        },
        bottomBar = {
          if (!useRail) {
            AnimatedVisibility(
              visible = !uiState.isFullscreen,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut(),
            ) {
              PortalBottomBar(
                canGoBack = uiState.canGoBack,
                canGoForward = uiState.canGoForward,
                isAtHome = uiState.currentUrl.startsWith(PORTAL_HOME_URL),
                onBack = { viewModel.goBack() },
                onForward = { viewModel.goForward() },
                onHome = { viewModel.loadHome() },
                onFullscreen = { viewModel.toggleFullscreen() },
                onShare = { shareUrl(context, uiState.pageTitle, uiState.currentUrl) },
              )
            }
          }
        },
      ) { innerPadding ->
        Row(
          modifier =
            Modifier
              .fillMaxSize()
              .padding(innerPadding),
        ) {
          if (useRail && !uiState.isFullscreen) {
            PortalSideRail(
              canGoBack = uiState.canGoBack,
              canGoForward = uiState.canGoForward,
              isAtHome = uiState.currentUrl.startsWith(PORTAL_HOME_URL),
              onBack = { viewModel.goBack() },
              onForward = { viewModel.goForward() },
              onHome = { viewModel.loadHome() },
              onFullscreen = { viewModel.toggleFullscreen() },
              onShare = { shareUrl(context, uiState.pageTitle, uiState.currentUrl) },
            )
          }

          Box(
            modifier =
              Modifier
                .weight(1f)
                .fillMaxHeight(),
          ) {
            PortalWebView(
              initialUrl = PORTAL_HOME_URL,
              textZoomPercent = uiState.preferences.textZoomPercent,
              desktopMode = uiState.preferences.desktopMode,
              commands = viewModel.commands,
              onPageStarted = viewModel::onPageStarted,
              onProgressChanged = viewModel::onProgressChanged,
              onReceivedTitle = viewModel::onReceivedTitle,
              onPageFinished = viewModel::onPageFinished,
              onNavigationStateChanged = viewModel::onNavigationStateChanged,
              onMainFrameError = viewModel::onMainFrameError,
              onMessage = { msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
              },
            )

            uiState.errorInfo?.let { error ->
              PortalErrorView(
                errorInfo = error,
                isOnline = uiState.isOnline,
                onRetry = { viewModel.retryCurrentPage() },
                onLoadHome = { viewModel.loadHome() },
                onOpenExternal = { url ->
                  openInExternalBrowser(context, url) {
                    scope.launch {
                      snackbarHostState.showSnackbar(context.getString(R.string.message_no_browser))
                    }
                  }
                },
              )
            }

            // Floating restore pill in Fullscreen Reader mode
            androidx.compose.animation.AnimatedVisibility(
              visible = uiState.isFullscreen,
              enter = fadeIn(),
              exit = fadeOut(),
              modifier =
                Modifier
                  .align(Alignment.BottomEnd)
                  .windowInsetsPadding(WindowInsets.safeDrawing)
                  .padding(16.dp),
            ) {
              Surface(
                onClick = { viewModel.exitFullscreen() },
                shape = CircleShape,
                color = BrandNavy.copy(alpha = 0.92f),
                contentColor = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.testTag("exit_fullscreen_pill"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.FullscreenExit,
                    contentDescription = stringResource(R.string.action_exit_fullscreen),
                    tint = BrandGold,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = stringResource(R.string.action_exit_fullscreen),
                    style = MaterialTheme.typography.labelMedium,
                  )
                }
              }
            }
          }
        }
      }
    }

    // Active Modal Bottom Sheets
    when (uiState.activeSheet) {
      PortalSheet.SETTINGS -> {
        PortalSettingsSheet(
          preferences = uiState.preferences,
          onTextZoomChange = viewModel::setTextZoomPercent,
          onDesktopModeChange = viewModel::setDesktopMode,
          onThemeModeChange = viewModel::setThemeMode,
          onDismiss = viewModel::dismissSheet,
        )
      }

      PortalSheet.ABOUT -> {
        PortalAboutSheet(
          currentUrl = uiState.currentUrl,
          onSharePortal = { shareUrl(context, uiState.pageTitle, uiState.currentUrl) },
          onOpenExternal = {
            openInExternalBrowser(context, uiState.currentUrl) {
              scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.message_no_browser))
              }
            }
          },
          onDismiss = viewModel::dismissSheet,
        )
      }

      null -> Unit
    }

    // Branded Splash Overlay on Cold Start
    SplashOverlay(
      visible = uiState.showSplash,
      progress = uiState.loadProgress,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortalTopBar(
  title: String,
  currentUrl: String,
  isOnline: Boolean,
  isLoading: Boolean,
  loadProgress: Int,
  onRefresh: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenAbout: () -> Unit,
  onToggleFullscreen: () -> Unit,
  onShare: () -> Unit,
  onOpenExternal: () -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  val displayHost =
    remember(currentUrl) {
      runCatching { Uri.parse(currentUrl).host }.getOrNull() ?: "poralekhahobe.page.gd"
    }
  val isSecure = currentUrl.startsWith("https://", ignoreCase = true)

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(BrandNavy)
        .statusBarsPadding(),
  ) {
    TopAppBar(
      windowInsets = WindowInsets(0, 0, 0, 0),
      colors =
        TopAppBarDefaults.topAppBarColors(
          containerColor = BrandNavy,
          titleContentColor = Color.White,
          actionIconContentColor = Color.White,
        ),
      navigationIcon = {
        Image(
          painter = painterResource(R.drawable.logo_pora_lekha_hobe),
          contentDescription = stringResource(R.string.cd_app_logo),
          modifier =
            Modifier
              .padding(start = 12.dp, end = 4.dp)
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .testTag("top_bar_logo"),
        )
      },
      title = {
        Column(modifier = Modifier.padding(start = 4.dp)) {
          Text(
            text = title.ifBlank { stringResource(R.string.app_name) },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector =
                when {
                  !isOnline -> Icons.Filled.WifiOff
                  isSecure -> Icons.Filled.Lock
                  else -> Icons.Filled.Public
                },
              contentDescription =
                stringResource(
                  when {
                    !isOnline -> R.string.cd_connection_offline
                    isSecure -> R.string.cd_connection_secure
                    else -> R.string.cd_connection_http
                  }
                ),
              tint = if (!isOnline) Color(0xFFFF8A80) else BrandGold,
              modifier = Modifier.size(12.dp),
            )
            Text(
              text =
                if (!isOnline) {
                  stringResource(R.string.status_offline_badge)
                } else {
                  displayHost
                },
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.8f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      },
      actions = {
        IconButton(
          onClick = onRefresh,
          modifier = Modifier.testTag("refresh_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = stringResource(R.string.action_refresh),
          )
        }

        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.testTag("settings_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = stringResource(R.string.action_reader_settings),
          )
        }

        Box {
          IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.testTag("overflow_menu_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.MoreVert,
              contentDescription = stringResource(R.string.action_about),
            )
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
          ) {
            DropdownMenuItem(
              text = { Text(stringResource(R.string.action_fullscreen)) },
              leadingIcon = { Icon(Icons.Filled.Fullscreen, contentDescription = null) },
              onClick = {
                menuExpanded = false
                onToggleFullscreen()
              },
              modifier = Modifier.testTag("menu_fullscreen"),
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.action_share)) },
              leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
              onClick = {
                menuExpanded = false
                onShare()
              },
              modifier = Modifier.testTag("menu_share"),
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.action_open_browser)) },
              leadingIcon = {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
              },
              onClick = {
                menuExpanded = false
                onOpenExternal()
              },
              modifier = Modifier.testTag("menu_open_browser"),
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.action_about)) },
              leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
              onClick = {
                menuExpanded = false
                onOpenAbout()
              },
              modifier = Modifier.testTag("menu_about"),
            )
          }
        }
      },
    )

    if (isLoading && loadProgress in 1..99) {
      LinearProgressIndicator(
        progress = { loadProgress / 100f },
        modifier =
          Modifier
            .fillMaxWidth()
            .height(3.dp),
        color = BrandGold,
        trackColor = BrandNavy,
      )
    } else {
      Spacer(modifier = Modifier.height(3.dp))
    }
  }
}

@Composable
private fun PortalBottomBar(
  canGoBack: Boolean,
  canGoForward: Boolean,
  isAtHome: Boolean,
  onBack: () -> Unit,
  onForward: () -> Unit,
  onHome: () -> Unit,
  onFullscreen: () -> Unit,
  onShare: () -> Unit,
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 4.dp,
  ) {
    NavigationBarItem(
      selected = false,
      enabled = canGoBack,
      onClick = onBack,
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.action_back),
        )
      },
      label = { Text(stringResource(R.string.action_back)) },
      modifier = Modifier.testTag("nav_back_button"),
    )

    NavigationBarItem(
      selected = false,
      enabled = canGoForward,
      onClick = onForward,
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = stringResource(R.string.action_forward),
        )
      },
      label = { Text(stringResource(R.string.action_forward)) },
      modifier = Modifier.testTag("nav_forward_button"),
    )

    NavigationBarItem(
      selected = isAtHome,
      onClick = onHome,
      icon = {
        Icon(
          imageVector = Icons.Filled.Home,
          contentDescription = stringResource(R.string.action_home),
        )
      },
      label = { Text(stringResource(R.string.action_home)) },
      colors =
        NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        ),
      modifier = Modifier.testTag("nav_home_button"),
    )

    NavigationBarItem(
      selected = false,
      onClick = onFullscreen,
      icon = {
        Icon(
          imageVector = Icons.Filled.Fullscreen,
          contentDescription = stringResource(R.string.action_fullscreen),
        )
      },
      label = { Text(stringResource(R.string.action_fullscreen), maxLines = 1) },
      modifier = Modifier.testTag("nav_fullscreen_button"),
    )

    NavigationBarItem(
      selected = false,
      onClick = onShare,
      icon = {
        Icon(
          imageVector = Icons.Filled.Share,
          contentDescription = stringResource(R.string.action_share),
        )
      },
      label = { Text(stringResource(R.string.action_share), maxLines = 1) },
      modifier = Modifier.testTag("nav_share_button"),
    )
  }
}

@Composable
private fun PortalSideRail(
  canGoBack: Boolean,
  canGoForward: Boolean,
  isAtHome: Boolean,
  onBack: () -> Unit,
  onForward: () -> Unit,
  onHome: () -> Unit,
  onFullscreen: () -> Unit,
  onShare: () -> Unit,
) {
  NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
    NavigationRailItem(
      selected = false,
      enabled = canGoBack,
      onClick = onBack,
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.action_back),
        )
      },
      label = { Text(stringResource(R.string.action_back)) },
    )
    NavigationRailItem(
      selected = false,
      enabled = canGoForward,
      onClick = onForward,
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = stringResource(R.string.action_forward),
        )
      },
      label = { Text(stringResource(R.string.action_forward)) },
    )
    NavigationRailItem(
      selected = isAtHome,
      onClick = onHome,
      icon = {
        Icon(
          imageVector = Icons.Filled.Home,
          contentDescription = stringResource(R.string.action_home),
        )
      },
      label = { Text(stringResource(R.string.action_home)) },
    )
    NavigationRailItem(
      selected = false,
      onClick = onFullscreen,
      icon = {
        Icon(
          imageVector = Icons.Filled.Fullscreen,
          contentDescription = stringResource(R.string.action_fullscreen),
        )
      },
      label = { Text(stringResource(R.string.action_fullscreen)) },
    )
    NavigationRailItem(
      selected = false,
      onClick = onShare,
      icon = {
        Icon(
          imageVector = Icons.Filled.Share,
          contentDescription = stringResource(R.string.action_share),
        )
      },
      label = { Text(stringResource(R.string.action_share)) },
    )
  }
}

private fun shareUrl(context: Context, title: String, url: String) {
  val sendIntent =
    Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, "$title\n$url")
    }
  val chooser = Intent.createChooser(sendIntent, context.getString(R.string.action_share))
  runCatching { context.startActivity(chooser) }
}

private fun openInExternalBrowser(context: Context, url: String, onFailure: () -> Unit) {
  runCatching {
    val intent =
      Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    context.startActivity(intent)
  }.onFailure { onFailure() }
}
