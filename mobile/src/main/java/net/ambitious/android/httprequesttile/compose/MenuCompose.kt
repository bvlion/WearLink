package net.ambitious.android.wearnetvoyage.compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.ambitious.android.wearnetvoyage.BuildConfig
import net.ambitious.android.wearnetvoyage.data.AppConstants
import net.ambitious.android.wearnetvoyage.data.RequestParams.Companion.parseRequestParams
import net.ambitious.android.wearnetvoyage.ui.theme.AppTheme
import net.ambitious.android.wearnetvoyage.ui.theme.noRippleClickable
import java.net.URLEncoder

@Composable
fun MenuList(
  bottomPadding: Dp = 56.dp,
  viewMode: AppConstants.ViewMode,
  saveViewMode: (AppConstants.ViewMode) -> Unit = {},
  syncWatch: () -> Unit = {},
  historyDelete: () -> Unit = {},
  savePasteRequest: (String) -> Unit = {},
  copyToClipboard: (Boolean) -> Unit = {},
  showRules: (String) -> Unit = {}
) {
  val context = LocalContext.current
  val isSystemInDarkTheme = isSystemInDarkTheme()

  val deleteClick = remember { mutableStateOf(false) }
  if (deleteClick.value) {
    AlertDialog(
      onDismissRequest = { deleteClick.value = false },
      title = { Text("実行履歴を削除しますか？") },
      text = { Text("全ての実行履歴が削除されます。\nこの操作は元に戻せません。") },
      dismissButton = {
        TextButton(onClick = { deleteClick.value = false }) { Text("閉じる") }
      },
      confirmButton = {
        TextButton(onClick = {
          deleteClick.value = false
          historyDelete()
        }) { Text("削除する") }
      }
    )
  }

  val pasteCheck = remember { mutableStateOf(false) }
  val pasteError = remember { mutableStateOf("") }
  val pasteText = remember { mutableStateOf("") }
  if (pasteCheck.value) {
    Dialog(
      onDismissRequest = { pasteCheck.value = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Surface(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        shape = RoundedCornerShape(8.dp),
      ) {
        Column(
          Modifier.fillMaxSize().padding(16.dp),
        ) {
          Text(
            text = "Request をインポート",
            modifier = Modifier.padding(bottom = 16.dp)
          )
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth().weight(1f),
            value = pasteText.value,
            onValueChange = { pasteText.value = it },
            label = { Text("Json 形式の Request") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { pasteCheck.value = false }),
            maxLines = Int.MAX_VALUE,
            isError = pasteError.value.isNotEmpty(),
            trailingIcon = {
              if (pasteError.value.isNotEmpty()) {
                Icon(Icons.Filled.Error, "入力エラー", tint = MaterialTheme.colors.error)
              }
            }
          )
          if (pasteError.value.isNotEmpty()) {
            Text(
              pasteError.value,
              color = MaterialTheme.colors.error,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            TextButton(
              onClick = {
                pasteCheck.value = false
                pasteText.value = ""
                pasteError.value = ""
              },
              modifier = Modifier.padding(end = 16.dp)
            ) { Text("閉じる") }
            Button(
              onClick = {
                if (pasteText.value.isBlank()) {
                  pasteError.value = "入力してください"
                  return@Button
                }

                try {
                  pasteText.value.parseRequestParams()
                } catch (e: Exception) {
                  pasteError.value = "Json の値が不正です。一度エクスポートしてフォーマット確認のうえ統一してください。"
                  return@Button
                }

                savePasteRequest(pasteText.value)
                pasteCheck.value = false
                pasteText.value = ""
                pasteError.value = ""
              }
            ) {
              Text("登録する")
            }
          }
        }
      }
    }
  }

  Column(
    Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 4.dp, top = 16.dp)
    ) {
      AppConstants.ViewMode.values().forEach { mode ->
        RadioButton(
          selected = (mode == viewMode),
          onClick = { saveViewMode(mode) }
        )
        Text(
          modifier = Modifier
            .padding(top = 12.dp)
            .noRippleClickable { saveViewMode(mode) },
          fontSize = 15.sp,
          style = MaterialTheme.typography.body1.merge(),
          text = when (mode) {
            AppConstants.ViewMode.DEFAULT -> "デフォルト"
            AppConstants.ViewMode.LIGHT -> "ライト"
            AppConstants.ViewMode.DARK -> "ダーク"
          }
        )
      }
    }

    MenuRow("ウェアラブルと同期", Icons.Filled.Sync) {
      syncWatch()
    }

    MenuRow("Request をエクスポート", Icons.Filled.Upload) {
      copyToClipboard(true)
    }

    MenuRow("Request をインポート", Icons.Filled.Download) {
      pasteCheck.value = true
    }

    MenuRow("実行履歴をエクスポート", Icons.Filled.Upload) {
      copyToClipboard(false)
    }

    MenuRow("実行履歴を削除", Icons.Filled.Delete) {
      deleteClick.value = true
    }

    MenuRow("利用規約", Icons.Filled.Description) {
      showRules(
        if (AppConstants.isDarkMode(viewMode, isSystemInDarkTheme)) {
          AppConstants.DARK_TERMS_OF_USE_URL
        } else {
          AppConstants.LIGHT_TERMS_OF_USE_URL
        }
      )
    }

    MenuRow("プライバシー・ポリシー", Icons.Filled.Description) {
      showRules(
        if (AppConstants.isDarkMode(viewMode, isSystemInDarkTheme)) {
          AppConstants.DARK_PRIVACY_POLICY_URL
        } else {
          AppConstants.LIGHT_PRIVACY_POLICY_URL
        }
      )
    }

    MenuRow("レビューする", Icons.Filled.RateReview) {
      context.startActivity(
        Intent(
          Intent.ACTION_VIEW,
          try {
            Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
          } catch (_: ActivityNotFoundException) {
            Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
          }
        )
      )
    }

    MenuRow("ご意見", Icons.Filled.ContactMail) {
      context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.INQUIRY_URL)))
    }

    Box(modifier = Modifier.padding(bottom = bottomPadding))
  }
}

@Composable
private fun MenuRow(dist: String, icon: ImageVector, click: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = click),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      icon,
      contentDescription = dist,
      modifier = Modifier.padding(start = 16.dp)
    )
    Text(text = dist, modifier = Modifier.padding(16.dp))
  }
}

@Composable
fun RulesDialogCompose(
  url: String,
  dismissClick: () -> Unit = {}
) {
  if (url.isNotEmpty()) {
    val loading = remember { mutableStateOf(true) }

    AlertDialog(
      onDismissRequest = dismissClick,
      text = {
        Box(
          Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          if (loading.value) {
            CircularProgressIndicator()
          }
          AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = ::WebView,
            update = {
              it.setBackgroundColor(Color.TRANSPARENT)
              it.settings.cacheMode = WebSettings.LOAD_NO_CACHE
              it.loadUrl(url)
              it.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                  super.onPageStarted(view, url, favicon)
                  loading.value = false
                }
              }
            }
          )
        }
      },
      properties = DialogProperties(usePlatformDefaultWidth = false),
      confirmButton = {
        TextButton(dismissClick) { Text("閉じる") }
      },
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun MenuListPreview() {
  AppTheme {
    MenuList(viewMode = AppConstants.ViewMode.DARK)
  }
}
