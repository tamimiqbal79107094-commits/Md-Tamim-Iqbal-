package com.example.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.PORTAL_HOME_URL
import com.example.ui.WebCommand
import kotlinx.coroutines.flow.SharedFlow

private const val DESKTOP_USER_AGENT =
  "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"

private val EXTERNAL_ONLY_HOSTS =
  setOf(
    "play.google.com",
    "wa.me",
    "api.whatsapp.com",
    "t.me",
  )

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PortalWebView(
  initialUrl: String = PORTAL_HOME_URL,
  textZoomPercent: Int,
  desktopMode: Boolean,
  commands: SharedFlow<WebCommand>,
  onPageStarted: (url: String?, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
  onProgressChanged: (Int) -> Unit,
  onReceivedTitle: (String?) -> Unit,
  onPageFinished: (url: String?, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
  onNavigationStateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
  onMainFrameError: (errorCode: Int, description: String, failingUrl: String) -> Unit,
  onMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  val currentOnPageStarted by rememberUpdatedState(onPageStarted)
  val currentOnProgressChanged by rememberUpdatedState(onProgressChanged)
  val currentOnReceivedTitle by rememberUpdatedState(onReceivedTitle)
  val currentOnPageFinished by rememberUpdatedState(onPageFinished)
  val currentOnNavChanged by rememberUpdatedState(onNavigationStateChanged)
  val currentOnError by rememberUpdatedState(onMainFrameError)
  val currentOnMessage by rememberUpdatedState(onMessage)

  var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
  var customVideoView by remember { mutableStateOf<View?>(null) }
  var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

  val fileChooserLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      val callback = filePathCallback
      filePathCallback = null
      if (callback == null) return@rememberLauncherForActivityResult

      val uris =
        if (result.resultCode == Activity.RESULT_OK) {
          WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        } else {
          null
        }
      callback.onReceiveValue(uris)
    }

  val defaultUserAgent = remember(context) { WebSettings.getDefaultUserAgent(context) }

  val webView =
    remember(context) {
      WebView(context).apply {
        layoutParams =
          ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
          )
        isFocusable = true
        isFocusableInTouchMode = true

        val webViewInstance = this
        CookieManager.getInstance().apply {
          setAcceptCookie(true)
          setAcceptThirdPartyCookies(webViewInstance, true)
        }

        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          databaseEnabled = true
          loadsImagesAutomatically = true
          useWideViewPort = true
          loadWithOverviewMode = true
          builtInZoomControls = true
          displayZoomControls = false
          setSupportZoom(true)
          allowFileAccess = true
          allowContentAccess = true
          mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
          mediaPlaybackRequiresUserGesture = false
          cacheMode = WebSettings.LOAD_DEFAULT
          textZoom = textZoomPercent
          userAgentString = if (desktopMode) DESKTOP_USER_AGENT else defaultUserAgent
        }

        setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
          val started =
            enqueueSystemDownload(
              context = context,
              url = url,
              userAgent = userAgent,
              contentDisposition = contentDisposition,
              mimeType = mimeType,
            )
          currentOnMessage(
            context.getString(
              if (started) R.string.message_download_started else R.string.message_download_failed
            )
          )
        }

        webViewClient =
          object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
              view: WebView?,
              request: WebResourceRequest?,
            ): Boolean {
              val uri = request?.url ?: return false
              val scheme = uri.scheme?.lowercase().orEmpty()
              val host = uri.host?.lowercase().orEmpty()

              if (scheme != "http" && scheme != "https") {
                openExternalUri(context, uri) {
                  currentOnMessage(context.getString(R.string.message_no_browser))
                }
                return true
              }

              if (EXTERNAL_ONLY_HOSTS.any { host == it || host.endsWith(".$it") }) {
                openExternalUri(context, uri) {
                  currentOnMessage(context.getString(R.string.message_no_browser))
                }
                return true
              }

              return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
              super.onPageStarted(view, url, favicon)
              currentOnPageStarted(url, view?.canGoBack() == true, view?.canGoForward() == true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
              super.onPageFinished(view, url)
              currentOnPageFinished(url, view?.canGoBack() == true, view?.canGoForward() == true)
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
              super.doUpdateVisitedHistory(view, url, isReload)
              currentOnNavChanged(view?.canGoBack() == true, view?.canGoForward() == true)
            }

            override fun onReceivedError(
              view: WebView?,
              request: WebResourceRequest?,
              error: WebResourceError?,
            ) {
              super.onReceivedError(view, request, error)
              if (request?.isForMainFrame == true) {
                val code = error?.errorCode ?: ERROR_UNKNOWN
                val desc = error?.description?.toString().orEmpty()
                val failingUrl = request.url?.toString() ?: initialUrl
                currentOnError(code, desc, failingUrl)
              }
            }
          }

        webChromeClient =
          object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
              super.onProgressChanged(view, newProgress)
              currentOnProgressChanged(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
              super.onReceivedTitle(view, title)
              currentOnReceivedTitle(title)
            }

            override fun onShowFileChooser(
              webView: WebView?,
              callback: ValueCallback<Array<Uri>>?,
              fileChooserParams: FileChooserParams?,
            ): Boolean {
              filePathCallback?.onReceiveValue(null)
              filePathCallback = callback
              val intent =
                runCatching { fileChooserParams?.createIntent() }
                  .getOrNull()
                  ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                  }
              return try {
                fileChooserLauncher.launch(intent)
                true
              } catch (_: Exception) {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = null
                false
              }
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
              if (customVideoView != null) {
                callback?.onCustomViewHidden()
                return
              }
              customVideoView = view
              customViewCallback = callback
            }

            override fun onHideCustomView() {
              customVideoView = null
              customViewCallback?.onCustomViewHidden()
              customViewCallback = null
            }
          }

        loadUrl(initialUrl)
      }
    }

  LaunchedEffect(textZoomPercent) {
    if (webView.settings.textZoom != textZoomPercent) {
      webView.settings.textZoom = textZoomPercent
    }
  }

  LaunchedEffect(desktopMode) {
    val targetUa = if (desktopMode) DESKTOP_USER_AGENT else defaultUserAgent
    if (webView.settings.userAgentString != targetUa) {
      webView.settings.userAgentString = targetUa
    }
  }

  LaunchedEffect(commands, webView) {
    commands.collect { command ->
      when (command) {
        WebCommand.GoBack -> if (webView.canGoBack()) webView.goBack()
        WebCommand.GoForward -> if (webView.canGoForward()) webView.goForward()
        WebCommand.Reload -> webView.reload()
        is WebCommand.LoadUrl -> webView.loadUrl(command.url)
      }
    }
  }

  DisposableEffect(webView) {
    onDispose {
      filePathCallback?.onReceiveValue(null)
      filePathCallback = null
      customViewCallback?.onCustomViewHidden()
      customViewCallback = null
      webView.stopLoading()
      webView.destroy()
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    AndroidView(
      factory = { webView },
      modifier = Modifier.fillMaxSize(),
    )

    customVideoView?.let { videoView ->
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(Color.Black),
      ) {
        AndroidView(
          factory = { ctx ->
            FrameLayout(ctx).apply {
              (videoView.parent as? ViewGroup)?.removeView(videoView)
              addView(
                videoView,
                FrameLayout.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT,
                ),
              )
            }
          },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

private fun openExternalUri(context: Context, uri: Uri, onFailure: () -> Unit) {
  runCatching {
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  }.onFailure { onFailure() }
}

private fun enqueueSystemDownload(
  context: Context,
  url: String,
  userAgent: String,
  contentDisposition: String,
  mimeType: String,
): Boolean {
  return runCatching {
    val uri = Uri.parse(url)
    val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
    val request =
      DownloadManager.Request(uri).apply {
        setMimeType(mimeType)
        addRequestHeader("User-Agent", userAgent)
        val cookies = CookieManager.getInstance().getCookie(url)
        if (!cookies.isNullOrBlank()) {
          addRequestHeader("Cookie", cookies)
        }
        setTitle(fileName)
        setDescription(context.getString(R.string.app_name))
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
      }
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
    true
  }.getOrElse {
    openExternalUri(context, Uri.parse(url)) {}
    false
  }
}
