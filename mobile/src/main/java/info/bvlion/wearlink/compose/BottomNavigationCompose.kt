package info.bvlion.wearlink.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import info.bvlion.wearlink.R
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