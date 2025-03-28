package info.bvlion.wearlink.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.data.ErrorDetail
import info.bvlion.wearlink.ui.theme.WearLinkTheme

@Composable
fun ErrorDialogCompose(err: ErrorDetail, dismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = dismiss,
    title = { Text(err.title) },
    text = { Text(err.message) },
    confirmButton = {
      TextButton(onClick = dismiss) { Text(stringResource(R.string.close)) }
    }
  )
}

@Composable
fun LoadingCompose() {
  Dialog(onDismissRequest = {}) {
    Box(
      Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator()
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ErrorDialogComposePreview() {
  WearLinkTheme {
    ErrorDetail(
      title = "Error",
      message = "Error message"
    ).let {
      ErrorDialogCompose(it) {}
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun LoadingComposePreview() {
  WearLinkTheme {
    LoadingCompose()
  }
}
