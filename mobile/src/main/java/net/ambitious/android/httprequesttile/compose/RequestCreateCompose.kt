package net.ambitious.android.wearnetvoyage.compose

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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.ambitious.android.wearnetvoyage.data.Constant
import net.ambitious.android.wearnetvoyage.data.RequestParams
import net.ambitious.android.wearnetvoyage.ui.theme.AppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RequestCreate(
  defaultTitle: String,
  defaultUrl: String,
  defaultMethod: Constant.HttpMethod,
  defaultBodyType: Constant.BodyType,
  defaultHeader: String,
  defaultBody: String,
  defaultWatchSync: Boolean,
  savedIndex: Int,
  bottomPadding: Dp = 56.dp,
  save: (Int, RequestParams) -> Unit = { _, _ -> },
  cancel: () -> Unit = {},
  delete: (Int) -> Unit = {},
) {
  val title = remember { mutableStateOf(defaultTitle) }
  val titleError = remember { mutableStateOf(false) }

  val url = remember { mutableStateOf(defaultUrl) }
  val urlError = remember { mutableStateOf(false) }

  val methodExpanded = remember { mutableStateOf(false) }
  val selectedMethod = remember { mutableStateOf(defaultMethod) }

  val bodyTypeExpanded = remember { mutableStateOf(false) }
  val selectedBodyType = remember { mutableStateOf(defaultBodyType) }

  val header = remember { mutableStateOf(defaultHeader) }
  val body = remember { mutableStateOf(defaultBody) }

  val editCheck = remember { mutableStateOf(false) }
  val cancelCheck = remember { mutableStateOf(false) }
  val deleteCheck = remember { mutableStateOf(false) }

  BackHandler {
    if (editCheck.value) {
      cancelCheck.value = true
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
        title.value = it
        titleError.value = it.isEmpty()
        editCheck.value = true
                      },
      label = { Text("タイトル") },
      modifier = Modifier
        .padding(
          top = 16.dp,
          start = 16.dp,
          end = 16.dp,
          bottom = if (titleError.value) 0.dp else 16.dp
        )
        .fillMaxWidth(),
      isError = titleError.value,
      trailingIcon = {
        if (titleError.value) {
          Icon(Icons.Filled.Error, "タイトル入力エラー", tint = MaterialTheme.colors.error)
        }
      }
    )
    if (titleError.value) {
      Text(
        "タイトルを入力してください",
        modifier = Modifier.padding(start = 32.dp, bottom = 16.dp),
        color = MaterialTheme.colors.error,
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
          label = { Text("Method") },
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(
              expanded = methodExpanded.value
            )
          },
          colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
          expanded = methodExpanded.value,
          onDismissRequest = {
            methodExpanded.value = false
          }
        ) {
          Constant.HttpMethod.values().forEach {
            DropdownMenuItem(onClick = {
              selectedMethod.value = it
              methodExpanded.value = false
              editCheck.value = true
            }) {
              Text(text = it.name)
            }
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
          label = { Text("BodyType") },
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(
              expanded = bodyTypeExpanded.value
            )
          },
          colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
          expanded = bodyTypeExpanded.value,
          onDismissRequest = {
            bodyTypeExpanded.value = false
          }
        ) {
          Constant.BodyType.values().forEach {
            DropdownMenuItem(onClick = {
              selectedBodyType.value = it
              bodyTypeExpanded.value = false
              editCheck.value = true
            }) {
              Text(text = it.name)
            }
          }
        }
      }
    }

    // URL入力欄
    OutlinedTextField(
      value = url.value,
      onValueChange = {
        url.value = it
        urlError.value = URLUtil.isValidUrl(it).not()
        editCheck.value = true
                      },
      label = { Text("URL") },
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
          Icon(Icons.Filled.Error, "URL 入力エラー", tint = MaterialTheme.colors.error)
        }
      }
    )
    if (urlError.value) {
      Text(
        "URL を入力してください",
        modifier = Modifier.padding(start = 32.dp, bottom = 8.dp),
        color = MaterialTheme.colors.error,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // ヘッダー入力欄
    OutlinedTextField(
      value = header.value,
      onValueChange = {
        header.value = it
        editCheck.value = true
                      },
      label = { Text("Request Headers") },
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
        body.value = it
        editCheck.value = true
                      },
      label = { Text("Request Body") },
      singleLine = false,
      minLines = 7,
      modifier = Modifier
        .padding(16.dp, 8.dp)
        .fillMaxWidth()
    )

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
            watchSync = defaultWatchSync
          )
        )
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp, 8.dp)
        .height(48.dp),
      enabled = title.value.isNotEmpty() && URLUtil.isValidUrl(url.value)
    ) {
      Text(text = if (savedIndex > -1) "更新" else "登録")
    }

    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp + bottomPadding)
    ) {
      OutlinedButton(
        onClick = {
          if (editCheck.value) {
            cancelCheck.value = true
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
        Icon(Icons.Filled.Close, contentDescription = "キャンセル")
        Text(text = "キャンセル", modifier = Modifier.padding(start = 8.dp))
      }

      if (savedIndex > -1) {
        OutlinedButton(
          onClick = { deleteCheck.value = true },
          modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp)
            .height(44.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colors.error
          )
        ) {
          Icon(Icons.Filled.Delete, contentDescription = "削除")
          Text(text = "削除", modifier = Modifier.padding(start = 8.dp))
        }
      }
    }

    if (cancelCheck.value) {
      AlertDialog(
        onDismissRequest = { cancelCheck.value = false },
        title = { Text("入力を中断しますか？") },
        text = { Text("中断すると今の入力内容は破棄されます。") },
        dismissButton = {
          TextButton(onClick = { cancelCheck.value = false }) { Text("閉じる") }
        },
        confirmButton = {
          TextButton(onClick = {
            cancelCheck.value = false
            cancel()
          }) { Text("入力をやめる") }
        }
      )
    }

    if (deleteCheck.value) {
      AlertDialog(
        onDismissRequest = { deleteCheck.value = false },
        title = { Text("削除しますか？") },
        text = { Text("「${title.value}」を削除します。\nこの操作は元に戻せません。") },
        dismissButton = {
          TextButton(onClick = { deleteCheck.value = false }) { Text("閉じる") }
        },
        confirmButton = {
          TextButton(onClick = {
            deleteCheck.value = false
            delete(savedIndex)
          }) { Text("削除") }
        }
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun RequestCreatePreview() {
  AppTheme {
    RequestCreate(
      "てすとだよ",
      "https://",
      Constant.HttpMethod.GET,
      Constant.BodyType.FORM_PARAMS,
      "Content-type:application/x-www-form-urlencoded\nUser-Agent:ワイのアプリ\n",
      "a=b",
      false,
      0
    )
  }
}
