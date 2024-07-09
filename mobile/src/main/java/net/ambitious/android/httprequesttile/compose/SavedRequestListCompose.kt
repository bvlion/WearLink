package net.ambitious.android.httprequesttile.compose

import androidx.compose.foundation.clickable
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
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.ambitious.android.httprequesttile.data.Constant
import net.ambitious.android.httprequesttile.data.RequestParams
import net.ambitious.android.httprequesttile.ui.theme.AppTheme
import net.ambitious.android.httprequesttile.ui.theme.noRippleClickable

@Composable
private fun SavedRequest(
  addTopPadding: Dp = 0.dp,
  addBottomPadding: Dp = 0.dp,
  requestParams: RequestParams,
  watchSync: (RequestParams) -> Boolean,
  edit: (RequestParams) -> Unit = {},
  send: (RequestParams) -> Unit = {},
) {
  val checked = remember { mutableStateOf(requestParams.watchSync) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp, 8.dp + addTopPadding, 8.dp, 8.dp + addBottomPadding)
      .clickable { edit(requestParams) },
    elevation = 2.dp,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .noRippleClickable {
              checked.value = !checked.value
              if (!watchSync(requestParams.copy(watchSync = checked.value))) {
                checked.value = false
              }
            }
            .padding(end = 16.dp)
        ) {
          Checkbox(checked = checked.value, onCheckedChange = {
            checked.value = it
            if (!watchSync(requestParams.copy(watchSync = checked.value))) {
              checked.value = false
            }
          })
          Text(text = "ウェアラブルに同期", fontSize = 12.sp)
        }
        Text(
          text = requestParams.title,
          modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 24.dp)
        )
      }
      IconButton(
        onClick = { send(requestParams) },
      ) {
        Icon(
          Icons.Default.Send,
          contentDescription = "送信ボタン",
          modifier = Modifier
            .height(80.dp)
            .width(80.dp)
            .padding(24.dp)
        )
      }
    }
  }
}

@Composable
fun SavedRequestList(
  requests: List<RequestParams>?,
  newCreateClick: () -> Unit = {},
  bottomPadding: Dp = 56.dp,
  watchSync: (Int, RequestParams) -> Boolean = { _, _ -> true },
  edit: (Int, RequestParams) -> Unit = { _, _ -> },
  send: (RequestParams) -> Unit = {},
) = when {
  requests == null -> Unit
  requests.isEmpty() -> Column(
    Modifier.fillMaxSize().padding(bottom = 24.dp + bottomPadding),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "保存されたリクエストがありません",
      fontSize = 13.sp
    )

    Button(
      onClick = newCreateClick,
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp)
        .height(48.dp),
    ) {
      Text(text = "新規リクエストを作成する")
    }
  }
  else -> Column(
    Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())) {
    requests.forEachIndexed { index, requestParams ->
      SavedRequest(
        if (index == 0) 8.dp else 0.dp,
        if (index == requests.lastIndex) 8.dp + bottomPadding else 0.dp,
        requestParams,
        watchSync = { watchSync(index, it) },
        edit = { edit(index, it) },
        send = send
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun SavedRequestListPreview() {
  AppTheme {
    SavedRequestList(listOf(
      RequestParams(
        "ぐーぐる",
        "https://www.google.com/",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
      )
    ))
  }
}
