package info.bvlion.wearlink.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.ui.theme.WearLinkTheme

@Composable
fun MenuBottomNavigation(
  selectedItem: MutableIntState,
  onSelected: (Int) -> Unit
) {
  NavigationBar {
    listOf(BottomItem.Home, BottomItem.Edit, BottomItem.History, BottomItem.Menu).forEachIndexed { index, item ->
      NavigationBarItem(
        icon = { Icon(item.icon, contentDescription = stringResource(item.titleResId)) },
        label = { Text(stringResource(item.titleResId)) },
        selected = selectedItem.intValue == index,
        onClick = {
          selectedItem.intValue = index
          onSelected(index)
        }
      )
    }
  }
}

@Composable
fun ReorderActionBar(onCancel: () -> Unit, onDone: () -> Unit) {
  BottomAppBar(modifier = Modifier.fillMaxWidth()) {
    TextButton(onClick = onCancel) {
      Text(stringResource(R.string.cancel))
    }
    Spacer(Modifier.weight(1f))
    TextButton(onClick = onDone) {
      Text(stringResource(R.string.saved_request_reorder_done))
    }
  }
}

@Composable
fun SelectExportActionBar(selectedCount: Int, onCancel: () -> Unit, onExport: () -> Unit) {
  BottomAppBar(modifier = Modifier.fillMaxWidth()) {
    TextButton(onClick = onCancel) {
      Text(stringResource(R.string.cancel))
    }
    Spacer(Modifier.weight(1f))
    Text(
      stringResource(R.string.saved_request_select_export_count, selectedCount),
      modifier = Modifier.padding(end = 8.dp)
    )
    TextButton(onClick = onExport, enabled = selectedCount > 0) {
      Text(stringResource(R.string.saved_request_select_export_done))
    }
  }
}

private sealed class BottomItem(val titleResId: Int, val icon: ImageVector) {
  data object Home : BottomItem(R.string.bottom_nav_home, Icons.Filled.Home)
  data object Edit : BottomItem(R.string.bottom_nav_edit, Icons.Filled.Edit)
  data object History : BottomItem(R.string.bottom_nav_history, Icons.Filled.HistoryEdu)
  data object Menu : BottomItem(R.string.bottom_nav_menu, Icons.Filled.Menu)
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationPreview() {
  WearLinkTheme {
    MenuBottomNavigation(remember { mutableIntStateOf(1) }) {}
  }
}

@Preview(showBackground = true)
@Composable
private fun ReorderActionBarPreview() {
  WearLinkTheme {
    ReorderActionBar({}, {})
  }
}

@Preview(showBackground = true)
@Composable
private fun SelectExportActionBarPreview() {
  WearLinkTheme {
    SelectExportActionBar(2, {}, {})
  }
}
