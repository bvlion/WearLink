package net.ambitious.android.httprequesttile.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import net.ambitious.android.httprequesttile.data.ErrorDetail

@Composable
fun ErrorDialogCompose(err: ErrorDetail?, dismiss: () -> Unit) {
  if (err != null) {
    AlertDialog(
      onDismissRequest = dismiss,
      title = { Text(err.title) },
      text = { Text(err.message) },
      confirmButton = {
        TextButton(onClick = dismiss) { Text("閉じる") }
      }
    )
  }
}

@Composable
fun LoadingCompose(iSLoading: Boolean) {
  if (iSLoading) {
    Box(
      Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .alpha(0.2f)
          .background(MaterialTheme.colors.onPrimary)
      )
      CircularProgressIndicator()
    }
  }
}
