package info.bvlion.wearlink.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Watch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
private fun StatusBadge(text: String, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors().copy(
      contentColor = MaterialTheme.colorScheme.primary,
      containerColor = MaterialTheme.colorScheme.primaryContainer
    )
  ) {
    Text(
      text = text,
      fontSize = 12.sp,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

@Composable
private fun SavedRequest(
  modifier: Modifier = Modifier,
  addTopPadding: Dp = 0.dp,
  addBottomPadding: Dp = 0.dp,
  requestParams: RequestParams,
  edit: (RequestParams) -> Unit = {},
  send: (RequestParams) -> Unit = {},
  toggleTile: (RequestParams) -> Unit = {},
  toggleWatchface: (RequestParams) -> Unit = {},
  addShortcut: (RequestParams) -> Unit = {},
  startReorder: () -> Unit = {},
  startSelect: () -> Unit = {},
) {
  var menuExpanded by remember { mutableStateOf(false) }

  val hasStatus = requestParams.watchSync || requestParams.watchfaceShortcut
  val titleVerticalPadding = if (hasStatus) 16.dp else 20.dp

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(8.dp, 8.dp + addTopPadding, 8.dp, 8.dp + addBottomPadding),
    elevation = CardDefaults.cardElevation(2.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(vertical = titleVerticalPadding, horizontal = 16.dp)
      ) {
        Text(text = requestParams.title, fontSize = 18.sp)
        if (hasStatus) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
          ) {
            if (requestParams.watchSync) {
              StatusBadge(stringResource(R.string.saved_request_tile_status))
            }
            if (requestParams.watchfaceShortcut) {
              StatusBadge(stringResource(R.string.saved_request_watchface_status))
            }
          }
        }
      }
      Box {
        IconButton(onClick = { menuExpanded = true }) {
          Icon(
            Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.saved_request_menu_button)
          )
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
          DropdownMenuItem(
            text = { Text(stringResource(R.string.saved_request_menu_edit)) },
            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            onClick = {
              menuExpanded = false
              edit(requestParams)
            }
          )
          DropdownMenuItem(
            text = {
              Text(
                stringResource(
                  if (requestParams.watchSync) {
                    R.string.saved_request_menu_hide_from_tile
                  } else {
                    R.string.saved_request_menu_show_on_tile
                  }
                )
              )
            },
            leadingIcon = { Icon(Icons.Filled.ViewCarousel, contentDescription = null) },
            onClick = {
              menuExpanded = false
              toggleTile(requestParams)
            }
          )
          DropdownMenuItem(
            text = {
              Text(
                stringResource(
                  if (requestParams.watchfaceShortcut) {
                    R.string.saved_request_menu_hide_from_watchface
                  } else {
                    R.string.saved_request_menu_show_on_watchface
                  }
                )
              )
            },
            leadingIcon = { Icon(Icons.Filled.Watch, contentDescription = null) },
            onClick = {
              menuExpanded = false
              toggleWatchface(requestParams)
            }
          )
          DropdownMenuItem(
            text = { Text(stringResource(R.string.saved_request_menu_add_shortcut)) },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.AddToHomeScreen, contentDescription = null) },
            onClick = {
              menuExpanded = false
              addShortcut(requestParams)
            }
          )
          HorizontalDivider()
          DropdownMenuItem(
            text = { Text(stringResource(R.string.saved_request_menu_reorder)) },
            leadingIcon = { Icon(Icons.Filled.Reorder, contentDescription = null) },
            onClick = {
              menuExpanded = false
              startReorder()
            }
          )
          DropdownMenuItem(
            text = { Text(stringResource(R.string.saved_request_menu_select_export)) },
            leadingIcon = { Icon(Icons.Filled.IosShare, contentDescription = null) },
            onClick = {
              menuExpanded = false
              startSelect()
            }
          )
        }
      }
      IconButton(
        onClick = { send(requestParams) },
        modifier = Modifier.padding(end = 8.dp)
      ) {
        Icon(
          Icons.AutoMirrored.Filled.Send,
          contentDescription = stringResource(R.string.saved_request_send_icon_tint)
        )
      }
    }
  }
}

@Composable
private fun SavedRequestReorderItem(
  modifier: Modifier = Modifier,
  addTopPadding: Dp = 0.dp,
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
      .padding(8.dp, 8.dp + addTopPadding, 8.dp, 8.dp + addBottomPadding),
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
            Icons.Filled.ViewCarousel,
            contentDescription = stringResource(R.string.saved_request_tile_status),
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
private fun SavedRequestSelectItem(
  modifier: Modifier = Modifier,
  addTopPadding: Dp = 0.dp,
  addBottomPadding: Dp = 0.dp,
  requestParams: RequestParams,
  selected: Boolean,
  toggleSelect: () -> Unit,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(8.dp, 8.dp + addTopPadding, 8.dp, 8.dp + addBottomPadding)
      .clickable(onClick = toggleSelect),
    elevation = CardDefaults.cardElevation(2.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(checked = selected, onCheckedChange = { toggleSelect() })
      Text(text = requestParams.title, fontSize = 18.sp)
    }
  }
}

@Composable
fun SavedRequestList(
  requests: List<RequestParams>,
  reorderMode: Boolean = false,
  selectMode: Boolean = false,
  selectedIds: Set<String> = emptySet(),
  newCreateClick: () -> Unit = {},
  topPadding: Dp = 8.dp,
  bottomPadding: Dp = 0.dp,
  edit: (RequestParams) -> Unit = {},
  send: (RequestParams) -> Unit = {},
  toggleTile: (RequestParams) -> Unit = {},
  toggleWatchface: (RequestParams) -> Unit = {},
  addShortcut: (RequestParams) -> Unit = {},
  startReorder: () -> Unit = {},
  moveUp: (RequestParams) -> Unit = {},
  moveDown: (RequestParams) -> Unit = {},
  startSelect: () -> Unit = {},
  toggleSelect: (RequestParams) -> Unit = {},
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
  else -> LazyColumn(Modifier.fillMaxWidth()) {
    itemsIndexed(requests, key = { _, requestParams -> requestParams.id }) { index, requestParams ->
      when {
        reorderMode -> SavedRequestReorderItem(
          modifier = Modifier.animateItem(),
          addTopPadding = if (index == 0) topPadding else 0.dp,
          addBottomPadding = if (index == requests.lastIndex) 8.dp + bottomPadding else 0.dp,
          requestParams = requestParams,
          canMoveUp = index != 0,
          canMoveDown = index != requests.lastIndex,
          moveUp = { moveUp(requestParams) },
          moveDown = { moveDown(requestParams) },
        )
        selectMode -> SavedRequestSelectItem(
          modifier = Modifier.animateItem(),
          addTopPadding = if (index == 0) topPadding else 0.dp,
          addBottomPadding = if (index == requests.lastIndex) 8.dp + bottomPadding else 0.dp,
          requestParams = requestParams,
          selected = requestParams.id in selectedIds,
          toggleSelect = { toggleSelect(requestParams) },
        )
        else -> SavedRequest(
          modifier = Modifier.animateItem(),
          addTopPadding = if (index == 0) topPadding else 0.dp,
          addBottomPadding = if (index == requests.lastIndex) 8.dp + bottomPadding else 0.dp,
          requestParams = requestParams,
          edit = edit,
          send = send,
          toggleTile = toggleTile,
          toggleWatchface = toggleWatchface,
          addShortcut = addShortcut,
          startReorder = startReorder,
          startSelect = startSelect,
        )
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
        "状態なし",
        "https://www.google.com/",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
      ),
      RequestParams(
        "タイルのみ",
        "https://www.google.com/",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
        watchSync = true
      ),
      RequestParams(
        "タイル + ウォッチフェイス",
        "https://www.google.com/",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
        watchSync = true,
        watchfaceShortcut = true
      )
    ))
  }
}
