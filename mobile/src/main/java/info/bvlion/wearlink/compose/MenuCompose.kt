package info.bvlion.wearlink.compose

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.ui.theme.WearLinkTheme
import info.bvlion.wearlink.ui.theme.noRippleClickable
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import info.bvlion.appinfomanager.changelog.ChangeLogManager
import info.bvlion.appinfomanager.contents.ContentsManager
import info.bvlion.wearlink.mobile.BuildConfig
import info.bvlion.wearlink.mobile.R

@Composable
fun MenuList(
  topPadding: Dp = 0.dp,
  bottomPadding: Dp = 0.dp,
  viewMode: AppConstants.ViewMode,
  saveViewMode: (AppConstants.ViewMode) -> Unit = {},
  syncWatch: () -> Unit = {},
  historyDelete: () -> Unit = {},
  savePasteRequest: (String) -> Unit = {},
  copyToClipboard: (Boolean) -> Unit = {},
) {
  val context = LocalContext.current

  Column(
    Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Spacer(modifier = Modifier.height(8.dp + topPadding))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 4.dp)
    ) {
      AppConstants.ViewMode.entries.forEach { mode ->
        RadioButton(
          selected = (mode == viewMode),
          onClick = { saveViewMode(mode) }
        )
        Text(
          modifier = Modifier
            .padding(top = 12.dp)
            .noRippleClickable { saveViewMode(mode) },
          fontSize = 15.sp,
          style = MaterialTheme.typography.bodyMedium.merge(),
          text = when (mode) {
            AppConstants.ViewMode.DEFAULT -> stringResource(R.string.view_mode_default)
            AppConstants.ViewMode.LIGHT -> stringResource(R.string.view_mode_light)
            AppConstants.ViewMode.DARK -> stringResource(R.string.view_mode_dark)
          }
        )
      }
    }

    MenuRow(stringResource(R.string.menu_title_sync_with_wearable), Icons.Filled.Sync) {
      syncWatch()
    }

    MenuRow(stringResource(R.string.menu_title_export_request), Icons.Filled.Upload) {
      copyToClipboard(true)
    }

    val shouldShowRequestImportDialogState = rememberSaveable { mutableStateOf(false) }
    if (shouldShowRequestImportDialogState.value) {
      RequestImportDialog(
        onDismiss = { shouldShowRequestImportDialogState.value = false },
        onConfirm = { savePasteRequest(it) }
      )
    }
    MenuRow(stringResource(R.string.menu_title_import_request), Icons.Filled.Download) {
      shouldShowRequestImportDialogState.value = true
    }

    MenuRow(stringResource(R.string.menu_title_export_execution_history), Icons.Filled.Upload) {
      copyToClipboard(false)
    }

    val shouldShowDeleteConfirmDialogState = rememberSaveable { mutableStateOf(false) }
    if (shouldShowDeleteConfirmDialogState.value) {
      DeleteExecuteHistoryConfirmDialog(
        onDismiss = { shouldShowDeleteConfirmDialogState.value = false },
        onConfirm = { historyDelete() }
      )
    }
    MenuRow(stringResource(R.string.menu_title_delete_execution_history), Icons.Filled.Delete) {
      shouldShowDeleteConfirmDialogState.value = true
    }

    val showChangeLog = rememberSaveable { mutableStateOf(false) }
    ChangeLogManager(FirebaseFirestore.getInstance(), context).ShowChangeLog(
      showChangeLog,
      BuildConfig.VERSION_NAME
    )
    MenuRow(stringResource(R.string.menu_title_change_log), Icons.Filled.History) {
      showChangeLog.value = true
    }

    val showTermsOfService = rememberSaveable { mutableStateOf(false) }
    ContentsManager(FirebaseFirestore.getInstance(), context).ShowTermsOfServiceDialog(
      showTermsOfService,
      AppConstants.isDarkMode(viewMode, AppConstants.isSystemInDarkTheme(context))
    )
    MenuRow(stringResource(R.string.menu_title_terms_of_use), Icons.Filled.Description) {
      showTermsOfService.value = true
    }

    val showPrivacyPolicy = rememberSaveable { mutableStateOf(false) }
    ContentsManager(FirebaseFirestore.getInstance(), context).ShowPrivacyPolicyDialog(
      showPrivacyPolicy,
      AppConstants.isDarkMode(viewMode, AppConstants.isSystemInDarkTheme(context))
    )
    MenuRow(stringResource(R.string.menu_title_privacy_policy), Icons.Filled.Description) {
      showPrivacyPolicy.value = true
    }

    MenuRow(stringResource(R.string.menu_title_review), Icons.Filled.Reviews) {
      context.startActivity(
        try {
          Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        } catch (_: ActivityNotFoundException) {
          Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${context.packageName}".toUri())
        }
      )
    }

    MenuRow(stringResource(R.string.menu_title_feedback), Icons.Filled.ContactMail) {
      context.startActivity(Intent(Intent.ACTION_VIEW, AppConstants.INQUIRY_URL.toUri()))
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        Icons.Filled.Sell,
        contentDescription = BuildConfig.VERSION_NAME,
        modifier = Modifier.padding(start = 16.dp)
      )
      Text(text = stringResource(R.string.menu_title_version, BuildConfig.VERSION_NAME), modifier = Modifier.padding(16.dp))
    }

    Spacer(modifier = Modifier.height(8.dp + bottomPadding))
  }
}

@Composable
private fun MenuRow(title: String, icon: ImageVector, click: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = click),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      icon,
      contentDescription = title,
      modifier = Modifier.padding(start = 16.dp)
    )
    Text(text = title, modifier = Modifier.padding(16.dp))
  }
}

@Composable
private fun DeleteExecuteHistoryConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.delete_execute_history_title)) },
    text = { Text(stringResource(R.string.delete_execute_history_description)) },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.close))
      }
    },
    confirmButton = {
      TextButton(onClick = {
        onDismiss()
        onConfirm()
      }) { Text(stringResource(R.string.delete)) }
    }
  )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun RequestImportDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
  val context = LocalContext.current
  val pasteError = rememberSaveable { mutableStateOf("") }
  val pasteText = rememberSaveable { mutableStateOf("") }
  val dismissAndReset = {
    onDismiss()
    pasteText.value = ""
    pasteError.value = ""
  }
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    BoxWithConstraints {
      val maxHeight = maxHeight * 0.85f
      val maxWidth = maxWidth * 0.9f
      Surface(
        modifier = Modifier.width(maxWidth).height(maxHeight),
        shape = RoundedCornerShape(8.dp),
      ) {
        Column(
          Modifier.fillMaxSize().padding(16.dp),
        ) {
          Text(
            text = stringResource(R.string.request_import_title),
            modifier = Modifier.padding(bottom = 16.dp)
          )
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth().weight(1f),
            value = pasteText.value,
            onValueChange = { pasteText.value = it },
            label = { Text(stringResource(R.string.request_import_input_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            maxLines = Int.MAX_VALUE,
            isError = pasteError.value.isNotEmpty(),
            trailingIcon = {
              if (pasteError.value.isNotEmpty()) {
                Icon(
                  Icons.Filled.Error,
                  stringResource(R.string.input_error),
                  tint = MaterialTheme.colorScheme.error
                )
              }
            }
          )
          if (pasteError.value.isNotEmpty()) {
            Text(
              pasteError.value,
              color = MaterialTheme.colorScheme.error,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
          ) {
            TextButton(
              onClick = dismissAndReset,
              modifier = Modifier.padding(end = 16.dp)
            ) { Text(stringResource(R.string.close)) }
            Button(
              onClick = {
                if (pasteText.value.isBlank()) {
                  pasteError.value = context.getString(R.string.request_import_input_error_blank)
                  return@Button
                }

                try {
                  pasteText.value.parseRequestParams()
                } catch (_: Exception) {
                  pasteError.value = context.getString(R.string.request_import_input_error_format)
                  return@Button
                }

                onConfirm(pasteText.value)
                dismissAndReset()
              }
            ) {
              Text(stringResource(R.string.create))
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun MenuListPreview() {
  WearLinkTheme {
    MenuList(viewMode = AppConstants.ViewMode.DARK)
  }
}

@Preview(showBackground = true)
@Composable
private fun DeleteExecuteHistoryConfirmDialogPreview() {
  WearLinkTheme {
    DeleteExecuteHistoryConfirmDialog({}, {})
  }
}

@Preview(showBackground = true)
@Composable
private fun RequestImportDialogPreview() {
  WearLinkTheme {
    RequestImportDialog({}, {})
  }
}
