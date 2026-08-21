package info.bvlion.wearlink.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.data.Constant
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.ui.theme.WearLinkTheme
import info.bvlion.wearlink.ui.theme.noRippleClickable

@Composable
private fun SavedRequest(
  modifier: Modifier = Modifier,
  addBottomPadding: Dp = 0.dp,
  requestParams: RequestParams,
  watchSync: (RequestParams) -> Unit,
  edit: (RequestParams) -> Unit = {},
  send: (RequestParams) -> Unit = {},
) {
  val toggleCheck = {
    watchSync(requestParams.copy(watchSync = !requestParams.watchSync))
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(8.dp, 8.dp, 8.dp, 8.dp + addBottomPadding)
      .clickable { edit(requestParams) },
    elevation = CardDefaults.cardElevation(2.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .noRippleClickable { toggleCheck() }
            .padding(end = 16.dp)
        ) {
          Checkbox(checked = requestParams.watchSync, onCheckedChange = { toggleCheck() })
          Text(text = stringResource(R.string.saved_request_sync_wearlable), fontSize = 12.sp)
        }
        Text(
          text = requestParams.title,
          fontSize = 18.sp,
          modifier = Modifier.padding(
            start = 16.dp,
            bottom = if (requestParams.watchfaceShortcut) {
              0.dp
            } else {
              20.dp
            }
          )
        )
        if (requestParams.watchfaceShortcut) {
          Card(
            modifier = Modifier.padding(top = 12.dp, start = 8.dp, bottom = 16.dp),
            colors = CardDefaults.cardColors().copy(
              contentColor = MaterialTheme.colorScheme.primary,
              containerColor = MaterialTheme.colorScheme.primaryContainer)
          ) {
            Text(
              text = stringResource(R.string.saved_request_set_watchface),
              fontSize = 14.sp,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
          }
        }
      }
      IconButton(
        onClick = { send(requestParams) },
        modifier = Modifier.padding(24.dp)
      ) {
        Icon(
          Icons.AutoMirrored.Filled.Send,
          contentDescription = stringResource(R.string.saved_request_send_icon_tint),
          modifier = Modifier.height(80.dp).width(80.dp)
        )
      }
    }
  }
}

@Composable
private fun SavedRequestReorderItem(
  modifier: Modifier = Modifier,
  addBottomPadding: Dp = 0.dp,
  requestParams: RequestParams,
  canMoveUp: Boolean,
  canMoveDown: Boolean,
  moveUp: () -> Unit,
  moveDown: () -> Unit,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(8.dp, 8.dp, 8.dp, 8.dp + addBottomPadding),
    elevation = CardDefaults.cardElevation(2.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        if (requestParams.watchSync) {
          Icon(
            Icons.Filled.Sync,
            contentDescription = stringResource(R.string.saved_request_sync_wearlable),
            modifier = Modifier.padding(end = 8.dp)
          )
        }
        Text(text = requestParams.title, fontSize = 18.sp)
      }
      IconButton(onClick = moveUp, enabled = canMoveUp) {
        Icon(
          Icons.Filled.KeyboardArrowUp,
          contentDescription = stringResource(R.string.saved_request_reorder_move_up)
        )
      }
      IconButton(onClick = moveDown, enabled = canMoveDown) {
        Icon(
          Icons.Filled.KeyboardArrowDown,
          contentDescription = stringResource(R.string.saved_request_reorder_move_down)
        )
      }
    }
  }
}

@Composable
fun SavedRequestList(
  requests: List<RequestParams>,
  newCreateClick: () -> Unit = {},
  topPadding: Dp = 8.dp,
  bottomPadding: Dp = 0.dp,
  watchSync: (Int, RequestParams) -> Unit = { _, _ -> },
  edit: (Int, RequestParams) -> Unit = { _, _ -> },
  send: (RequestParams) -> Unit = {},
  moveUp: (RequestParams) -> Unit = {},
  moveDown: (RequestParams) -> Unit = {},
) = when {
  requests.isEmpty() -> Column(
    Modifier.fillMaxSize().padding(bottom = 24.dp + bottomPadding),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = stringResource(R.string.saved_request_empty_message),
      fontSize = 13.sp
    )

    Button(
      onClick = newCreateClick,
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp)
        .height(48.dp),
    ) {
      Text(text = stringResource(R.string.saved_request_empty_create_button))
    }
  }
  else -> {
    var reorderMode by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(top = topPadding)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
        horizontalArrangement = Arrangement.End
      ) {
        TextButton(onClick = { reorderMode = !reorderMode }) {
          Icon(
            if (reorderMode) Icons.Filled.Check else Icons.Filled.SwapVert,
            contentDescription = null,
            modifier = Modifier.padding(end = 4.dp)
          )
          Text(
            text = stringResource(
              if (reorderMode) R.string.saved_request_reorder_done else R.string.saved_request_reorder_start
            )
          )
        }
      }
      LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
        itemsIndexed(requests, key = { _, requestParams -> requestParams.id }) { index, requestParams ->
          if (reorderMode) {
            SavedRequestReorderItem(
              modifier = Modifier.animateItem(),
              addBottomPadding = if (index == requests.lastIndex) 8.dp + bottomPadding else 0.dp,
              requestParams = requestParams,
              canMoveUp = index != 0,
              canMoveDown = index != requests.lastIndex,
              moveUp = { moveUp(requestParams) },
              moveDown = { moveDown(requestParams) },
            )
          } else {
            SavedRequest(
              modifier = Modifier.animateItem(),
              addBottomPadding = if (index == requests.lastIndex) 8.dp + bottomPadding else 0.dp,
              requestParams = requestParams,
              watchSync = { watchSync(index, it) },
              edit = { edit(index, it) },
              send = send
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun SavedRequestListEmptyPreview() {
  WearLinkTheme {
    SavedRequestList(emptyList())
  }
}

@Preview(showBackground = true)
@Composable
fun SavedRequestListHasItemPreview() {
  WearLinkTheme {
    SavedRequestList(listOf(
      RequestParams(
        "ぐーぐる",
        "https://www.google.com/",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
        watchfaceShortcut = true
      ),
      RequestParams(
        "ぐーぐる",
        "https://www.google.com/",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
        watchSync = true
      )
    ))
  }
}