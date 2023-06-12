package net.ambitious.android.httprequesttile.compose

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import net.ambitious.android.httprequesttile.ui.theme.MyApplicationTheme

@Composable
fun MenuBottomNavigation(selectedItem: MutableState<Int> = remember { mutableStateOf(0) }) {
  BottomNavigation {
    items.forEachIndexed { index, item ->
      BottomNavigationItem(
        icon = { Icon(item.icon, contentDescription = item.dist) },
        label = { Text(item.dist) },
        alwaysShowLabel = false,
        selected = selectedItem.value == index,
        onClick = { selectedItem.value = index }
      )
    }
  }
}

private val items = listOf(BottomItem.Home, BottomItem.Edit, BottomItem.History, BottomItem.Sync, BottomItem.Menu)

sealed class BottomItem(val dist: String, val icon: ImageVector) {
  object Home : BottomItem("ホーム", Icons.Filled.Home)
  object Edit : BottomItem("作成", Icons.Filled.Edit)
  object History : BottomItem("履歴", Icons.Filled.List)
  object Sync : BottomItem("同期", Icons.Filled.Refresh)
  object Menu : BottomItem("メニュー", Icons.Filled.Menu)
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
  MyApplicationTheme {
    MenuBottomNavigation()
  }
}