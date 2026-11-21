package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UserPreferences
import com.example.ui.PORTAL_HOME_URL
import com.example.ui.WebErrorInfo
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.BrandNavyDeep
import com.example.ui.theme.BrandRoyalBlue
import com.example.ui.theme.ThemeMode
import kotlin.math.roundToInt

@Composable
fun SplashOverlay(
  visible: Boolean,
  progress: Int,
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(tween(180)),
    exit = fadeOut(tween(320, easing = FastOutSlowInEasing)),
  ) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val logoScale by
      animateFloatAsState(
        targetValue = if (started) 1f else 0.86f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "splash_logo_scale",
      )

    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(BrandNavyDeep, BrandNavy, BrandRoyalBlue)
            )
          ),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(32.dp),
      ) {
        Image(
          painter = painterResource(R.drawable.logo_pora_lekha_hobe),
          contentDescription = stringResource(R.string.cd_app_logo),
          contentScale = ContentScale.Fit,
          modifier =
            Modifier
              .size(168.dp)
              .graphicsLayer {
                scaleX = logoScale
                scaleY = logoScale
              }
              .shadow(18.dp, RoundedCornerShape(36.dp), clip = false)
              .clip(RoundedCornerShape(36.dp)),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = BrandGold,
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
          progress = { (progress.coerceIn(10, 100)) / 100f },
          modifier =
            Modifier
              .width(180.dp)
              .height(6.dp)
              .clip(CircleShape),
          color = BrandGold,
          trackColor = Color.White.copy(alpha = 0.2f),
          strokeCap = StrokeCap.Round,
        )
      }
    }
  }
}

@Composable
fun PortalErrorView(
  errorInfo: WebErrorInfo,
  isOnline: Boolean,
  onRetry: () -> Unit,
  onLoadHome: () -> Unit,
  onOpenExternal: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth()
          .widthIn(max = 460.dp),
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 4.dp,
      shadowElevation = 8.dp,
    ) {
      Column(
        modifier =
          Modifier
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Box(contentAlignment = Alignment.BottomEnd) {
          Image(
            painter = painterResource(R.drawable.logo_pora_lekha_hobe),
            contentDescription = stringResource(R.string.cd_app_logo),
            modifier =
              Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(24.dp)),
          )
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            modifier =
              Modifier
                .size(34.dp)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isOnline) Icons.Filled.CloudOff else Icons.Filled.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }

        Text(
          text =
            stringResource(
              if (isOnline) R.string.error_page_title else R.string.error_offline_title
            ),
          style = MaterialTheme.typography.titleLarge,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
          text =
            if (!isOnline) {
              stringResource(R.string.error_offline_message)
            } else if (errorInfo.description.isNotBlank()) {
              "${stringResource(R.string.error_page_fallback)}\n(${errorInfo.description})"
            } else {
              stringResource(R.string.error_page_fallback)
            },
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
          onClick = onRetry,
          modifier =
            Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("retry_connection_button"),
          shape = RoundedCornerShape(14.dp),
        ) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = stringResource(R.string.action_retry))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          FilledTonalButton(
            onClick = onLoadHome,
            modifier =
              Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("error_home_button"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Home,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.action_home),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          OutlinedButton(
            onClick = { onOpenExternal(errorInfo.failingUrl) },
            modifier =
              Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("error_open_browser_button"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.action_open_browser),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalSettingsSheet(
  preferences: UserPreferences,
  onTextZoomChange: (Int) -> Unit,
  onDesktopModeChange: (Boolean) -> Unit,
  onThemeModeChange: (ThemeMode) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 24.dp)
          .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Text(
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )

      // Text Zoom Control
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.FormatSize,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = stringResource(R.string.settings_text_zoom),
              style = MaterialTheme.typography.titleSmall,
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${preferences.textZoomPercent}%",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
            )
            if (preferences.textZoomPercent != 100) {
              TextButton(
                onClick = { onTextZoomChange(100) },
                modifier = Modifier.testTag("reset_zoom_button"),
              ) {
                Text(text = stringResource(R.string.settings_reset_zoom))
              }
            }
          }
        }

        Slider(
          value = preferences.textZoomPercent.toFloat(),
          onValueChange = { raw ->
            val stepped = ((raw / 10f).roundToInt() * 10).coerceIn(80, 150)
            onTextZoomChange(stepped)
          },
          valueRange = 80f..150f,
          steps = 6,
          colors =
            SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
          modifier = Modifier.testTag("text_zoom_slider"),
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

      // Desktop Mode Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier = Modifier.weight(1f).padding(end = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.DesktopWindows,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Column {
            Text(
              text = stringResource(R.string.settings_desktop_mode),
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text = stringResource(R.string.settings_desktop_mode_desc),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Switch(
          checked = preferences.desktopMode,
          onCheckedChange = onDesktopModeChange,
          modifier = Modifier.testTag("desktop_mode_switch"),
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

      // Appearance Selector
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = stringResource(R.string.settings_theme_label),
            style = MaterialTheme.typography.titleSmall,
          )
        }

        val options =
          listOf(
            ThemeMode.SYSTEM to R.string.settings_theme_system,
            ThemeMode.LIGHT to R.string.settings_theme_light,
            ThemeMode.DARK to R.string.settings_theme_dark,
          )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          options.forEachIndexed { index, (mode, labelRes) ->
            SegmentedButton(
              selected = preferences.themeMode == mode,
              onClick = { onThemeModeChange(mode) },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
              modifier = Modifier.testTag("theme_option_${mode.name.lowercase()}"),
            ) {
              Text(text = stringResource(labelRes))
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalAboutSheet(
  currentUrl: String,
  onSharePortal: () -> Unit,
  onOpenExternal: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 24.dp)
          .padding(bottom = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Image(
        painter = painterResource(R.drawable.logo_pora_lekha_hobe),
        contentDescription = stringResource(R.string.cd_app_logo),
        modifier =
          Modifier
            .size(112.dp)
            .shadow(10.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp)),
      )

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = stringResource(R.string.app_tagline),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.secondary,
        )
      }

      Text(
        text = stringResource(R.string.about_description),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.Language,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.about_portal_label),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = PORTAL_HOME_URL,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Button(
          onClick = {
            onSharePortal()
            onDismiss()
          },
          modifier =
            Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("about_share_button"),
          shape = RoundedCornerShape(14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
          Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = stringResource(R.string.action_share))
        }

        OutlinedButton(
          onClick = {
            onOpenExternal()
            onDismiss()
          },
          modifier =
            Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("about_browser_button"),
          shape = RoundedCornerShape(14.dp),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.action_open_browser),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      if (currentUrl != PORTAL_HOME_URL) {
        Text(
          text = currentUrl,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
