package info.bvlion.wearlink.compose

import android.webkit.URLUtil
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.data.Constant
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.ui.theme.WearLinkTheme
import info.bvlion.wearlink.ui.theme.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestCreate(
  defaultTitle: String,
  defaultUrl: String,
  defaultMethod: Constant.HttpMethod,
  defaultBodyType: Constant.BodyType,
  defaultHeader: String,
  defaultBody: String,
  defaultWatchSync: Boolean,
  defaultWatchfaceShortcut: Boolean,
  savedIndex: Int,
  topPadding: Dp = 0.dp,
  bottomPadding: Dp = 0.dp,
  save: (Int, RequestParams) -> Unit = { _, _ -> },
  cancel: () -> Unit = {},
  delete: (Int) -> Unit = {},
) {
  val title = rememberSaveable { mutableStateOf(defaultTitle) }
  val titleError = rememberSaveable { mutableStateOf(false) }

  val url = rememberSaveable { mutableStateOf(defaultUrl) }
  val urlError = rememberSaveable { mutableStateOf(false) }

  val methodExpanded = remember { mutableStateOf(false) }
  val selectedMethod = rememberSaveable { mutableStateOf(defaultMethod) }

  val bodyTypeExpanded = remember { mutableStateOf(false) }
  val selectedBodyType = rememberSaveable { mutableStateOf(defaultBodyType) }

  val header = rememberSaveable { mutableStateOf(defaultHeader) }
  val body = rememberSaveable { mutableStateOf(defaultBody) }
  val watchfaceShortcut = rememberSaveable { mutableStateOf(defaultWatchfaceShortcut) }

  val editCheck = rememberSaveable { mutableStateOf(false) }
  val showCancelDialog = rememberSaveable { mutableStateOf(false) }
  val showDeleteDialog = rememberSaveable { mutableStateOf(false) }

  val updateEditCheck: (update: () -> Unit) -> Unit = { update ->
    update()
    editCheck.value = true
  }
  val toggleCheck = {
    updateEditCheck {
      watchfaceShortcut.value = !watchfaceShortcut.value
    }
  }


  BackHandler {
    if (editCheck.value) {
      showCancelDialog.value = true
      return@BackHandler
    }
    cancel()
  }

  Column(
    Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())) {

    // タイトル入力欄
    OutlinedTextField(
      value = title.value,
      onValueChange = {
        updateEditCheck {
          title.value = it
          titleError.value = it.isEmpty()
        }
      },
      label = { Text(stringResource(R.string.request_edit_input_title)) },
      modifier = Modifier
        .padding(
          top = topPadding,
          start = 16.dp,
          end = 16.dp,
          bottom = if (titleError.value) 0.dp else 16.dp
        )
        .fillMaxWidth(),
      isError = titleError.value,
      trailingIcon = {
        if (titleError.value) {
          Icon(Icons.Filled.Error, stringResource(R.string.request_edit_input_title_error_tint), tint = MaterialTheme.colorScheme.error)
        }
      }
    )
    if (titleError.value) {
      Text(
        stringResource(R.string.request_edit_input_title_error_blank),
        modifier = Modifier.padding(start = 32.dp, bottom = 16.dp),
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // セレクトボックス
    Row {
      // メソッドのセレクト
      ExposedDropdownMenuBox(
        expanded = methodExpanded.value,
        onExpandedChange = {
          methodExpanded.value = !methodExpanded.value
        },
        modifier = Modifier
          .width(160.dp)
          .padding(start = 16.dp, end = 8.dp)
      ) {
        TextField(
          readOnly = true,
          value = selectedMethod.value.name,
          onValueChange = {},
          label = { Text(stringResource(R.string.request_edit_select_method)) },
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(
              expanded = methodExpanded.value
            )
          },
          colors = ExposedDropdownMenuDefaults.textFieldColors(),
          modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
          expanded = methodExpanded.value,
          onDismissRequest = {
            methodExpanded.value = false
          }
        ) {
          Constant.HttpMethod.entries.forEach {
            DropdownMenuItem(
              onClick = {
                updateEditCheck {
                  selectedMethod.value = it
                  methodExpanded.value = false
                }
              },
              text = { Text(text = it.name) }
            )
          }
        }
      }

      // ボディタイプのセレクト
      ExposedDropdownMenuBox(
        expanded = bodyTypeExpanded.value,
        onExpandedChange = {
          bodyTypeExpanded.value = !bodyTypeExpanded.value
        },
        modifier = Modifier.padding(end = 16.dp, start = 8.dp)
      ) {
        TextField(
          readOnly = true,
          value = selectedBodyType.value.name,
          onValueChange = {},
          label = { Text(stringResource(R.string.request_edit_select_body_type)) },
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(
              expanded = bodyTypeExpanded.value
            )
          },
          colors = ExposedDropdownMenuDefaults.textFieldColors(),
          modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
          expanded = bodyTypeExpanded.value,
          onDismissRequest = {
            bodyTypeExpanded.value = false
          }
        ) {
          Constant.BodyType.entries.forEach {
            DropdownMenuItem(
              onClick = {
                updateEditCheck {
                  selectedBodyType.value = it
                  bodyTypeExpanded.value = false
                }
              }, text = {
                Text(text = it.name)
              }
            )
          }
        }
      }
    }

    // URL入力欄
    OutlinedTextField(
      value = url.value,
      onValueChange = {
        updateEditCheck {
          url.value = it
          urlError.value = URLUtil.isValidUrl(it).not()
        }
      },
      label = { Text(stringResource(R.string.request_edit_input_url)) },
      modifier = Modifier
        .padding(
          top = 16.dp,
          start = 16.dp,
          end = 16.dp,
          bottom = if (urlError.value) 0.dp else 8.dp
        )
        .fillMaxWidth(),
      isError = urlError.value,
      trailingIcon = {
        if (urlError.value) {
          Icon(Icons.Filled.Error, stringResource(R.string.request_edit_input_url_error_tint), tint = MaterialTheme.colorScheme.error)
        }
      }
    )
    if (urlError.value) {
      Text(
        stringResource(R.string.request_edit_input_url_error_blank),
        modifier = Modifier.padding(start = 32.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // ヘッダー入力欄
    OutlinedTextField(
      value = header.value,
      onValueChange = {
        updateEditCheck {
          header.value = it
        }
      },
      label = { Text(stringResource(R.string.request_edit_input_request_headers)) },
      singleLine = false,
      minLines = 7,
      modifier = Modifier
        .padding(16.dp, 8.dp)
        .fillMaxWidth()
    )

    // ボディ入力欄
    OutlinedTextField(
      value = body.value,
      onValueChange = {
        updateEditCheck {
          body.value = it
        }
      },
      label = { Text(stringResource(R.string.request_edit_input_request_body)) },
      singleLine = false,
      minLines = 7,
      modifier = Modifier
        .padding(16.dp, 8.dp)
        .fillMaxWidth()
    )

    // ウォッチフェイスショートカットのチェックボックス
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .noRippleClickable { toggleCheck() }
        .padding(start = 8.dp, end = 16.dp, bottom = 8.dp)
    ) {
      Checkbox(
        checked = watchfaceShortcut.value,
        onCheckedChange = {
          toggleCheck()
        }
      )
      Text(
        text = stringResource(R.string.request_edit_watchface_shortcut),
        fontSize = 14.sp
      )
    }

    Button(
      onClick = {
        if (title.value.isEmpty()) {
          titleError.value = true
        }
        if (url.value.isEmpty()) {
          urlError.value = true
        }
        if (titleError.value || urlError.value) {
          return@Button
        }

        save(
          savedIndex,
          RequestParams(
            title = title.value,
            url = url.value,
            method = selectedMethod.value,
            bodyType = selectedBodyType.value,
            headers = header.value,
            parameters = body.value,
            watchSync = defaultWatchSync,
            watchfaceShortcut = watchfaceShortcut.value,
          )
        )
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp, 8.dp)
        .height(48.dp),
      enabled = title.value.isNotEmpty() && URLUtil.isValidUrl(url.value)
    ) {
      Text(text = if (savedIndex > -1) stringResource(R.string.update) else stringResource(R.string.create))
    }

    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp + bottomPadding)
    ) {
      OutlinedButton(
        onClick = {
          if (editCheck.value) {
            showCancelDialog.value = true
            return@OutlinedButton
          }
          cancel()
        },
        modifier = Modifier
          .weight(1f)
          .padding(
            end = if (savedIndex > -1) {
              8.dp
            } else {
              0.dp
            }
          )
          .height(44.dp),
      ) {
        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
        Text(text = stringResource(R.string.cancel), modifier = Modifier.padding(start = 8.dp))
      }

      if (savedIndex > -1) {
        OutlinedButton(
          onClick = { showDeleteDialog.value = true },
          modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp)
            .height(44.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
          )
        ) {
          Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
          Text(text = stringResource(R.string.delete), modifier = Modifier.padding(start = 8.dp))
        }
      }
    }

    if (showCancelDialog.value) {
      InterruptConfirmDialog({ showCancelDialog.value = false }) {
        cancel()
      }
    }

    if (showDeleteDialog.value) {
      DeleteConfirmDialog(title.value, { showDeleteDialog.value = false }) {
        delete(savedIndex)
      }
    }
  }
}

@Composable
private fun InterruptConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.request_edit_interrupt_title)) },
    text = { Text(stringResource(R.string.request_edit_interrupt_description)) },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
    },
    confirmButton = {
      TextButton(onClick = {
        onDismiss()
        onConfirm()
      }) {
        Text(stringResource(R.string.request_edit_interrupt_confirm))
      }
    }
  )
}

@Composable
private fun DeleteConfirmDialog(title: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.request_edit_delete_title)) },
    text = { Text(stringResource(R.string.request_edit_delete_description, title)) },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
    },
    confirmButton = {
      TextButton(onClick = {
        onDismiss()
        onConfirm()
      }) {
        Text(stringResource(R.string.delete))
      }
    }
  )
}

@Preview(showBackground = true)
@Composable
private fun InterruptConfirmDialogPreview() {
  WearLinkTheme {
    InterruptConfirmDialog({}, {})
  }
}

@Preview(showBackground = true)
@Composable
private fun DeleteConfirmDialogPreview() {
  WearLinkTheme {
    DeleteConfirmDialog("てすとだよ", {}, {})
  }
}

@Preview(showBackground = true)
@Composable
private fun RequestCreatePreview() {
  WearLinkTheme {
    RequestCreate(
      "てすとだよ",
      "https://",
      Constant.HttpMethod.GET,
      Constant.BodyType.FORM_PARAMS,
      "Content-type:application/x-www-form-urlencoded\nUser-Agent:ワイのアプリ\n",
      "a=b",
      false,
      false,
      0
    )
  }
}