package net.ambitious.android.httprequesttile.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.ambitious.android.httprequesttile.data.ResponseParams
import net.ambitious.android.httprequesttile.ui.theme.AppTheme
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RequestHistory(
  responseParams: ResponseParams,
  addTopPadding: Dp = 0.dp,
  addBottomPadding: Dp = 0.dp,
  modalBottomSheetShow: (ResponseParams) -> Unit,
) {
  Card(
    modifier = Modifier
      .clickable { modalBottomSheetShow(responseParams) }
      .fillMaxWidth()
      .padding(8.dp, 8.dp + addTopPadding, 8.dp, 8.dp + addBottomPadding),
    elevation = 2.dp,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.padding(start = 16.dp)) {
        Text(
          text = if (responseParams.isMobile) "モバイルから送信" else "ウェアラブルから送信",
          fontSize = 12.sp
        )
        Text(
          text = responseParams.title,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.End
      ) {
        Text(
          text = "Response: ${responseParams.responseCode}",
          fontSize = 14.sp
        )
        Text(
          text = getSendDateTime(responseParams.sendDateTime),
          modifier = Modifier.padding(top = 8.dp),
          fontSize = 12.sp
        )
      }
    }
  }
}

@Composable
fun RequestHistoryList(
  responses: List<ResponseParams>,
  bottomPadding: Dp = 56.dp,
  modalBottomSheetShow: (ResponseParams) -> Unit = {},
) {
  if (responses.isEmpty()) {
    Column(
      Modifier
        .fillMaxSize()
        .padding(bottom = 24.dp + bottomPadding),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "送信履歴がありません",
        fontSize = 13.sp
      )
    }
  } else {
    Column(
      Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
      responses.forEachIndexed { index, response ->
        RequestHistory(
          response,
          if (index == 0) 8.dp else 0.dp,
          if (index == responses.lastIndex) 8.dp + bottomPadding else 0.dp,
          modalBottomSheetShow
        )
      }
    }
  }
}

@Composable
fun RequestHistoryDetailContent(responseParams: ResponseParams, bottomPadding: Dp = 56.dp) {
  Column(modifier = Modifier.padding(16.dp, 24.dp, 16.dp, 0.dp).verticalScroll(rememberScrollState())) {
    Text(
      text = getSendDateTime(responseParams.sendDateTime),
      modifier = Modifier.padding(end = 8.dp),
      fontSize = 14.sp
    )
    Text(responseParams.title)
    Row(Modifier.padding(top = 16.dp)) {
      Text(
        text = "Response Code:",
        modifier = Modifier.padding(end = 8.dp),
        fontSize = 14.sp
      )
      Text(responseParams.responseCode.toString())
    }
    Row(Modifier.padding(top = 16.dp)) {
      Text(
        text = "実行時間:",
        modifier = Modifier.padding(end = 8.dp),
        fontSize = 14.sp
      )
      Text("${NumberFormat.getNumberInstance().format(responseParams.execTime)} msec")
    }
    Text(
      text = "Response Body:",
      modifier = Modifier.padding(top = 16.dp),
      fontSize = 14.sp
    )
    Text(
      text = responseParams.body,
      modifier = Modifier.padding(top = 8.dp),
    )
    Text(
      text = "Response Header:",
      modifier = Modifier.padding(top = 16.dp),
      fontSize = 12.sp
    )
    Text(
      text = responseParams.header,
      modifier = Modifier.padding(top = 8.dp, bottom = 24.dp + bottomPadding),
    )
  }
}

private fun getSendDateTime(dateTime: Long): String =
  Instant.ofEpochMilli(dateTime)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))

@Preview(showBackground = true)
@Composable
@ExperimentalMaterialApi
fun RequestHistoryListPreview() {
  val response = ResponseParams(
    "HogeHoge の Request",
    200,
    45,
    "aaaa",
    "bbbb",
    System.currentTimeMillis(),
    false
  )
  AppTheme {
    ModalBottomSheetLayout(
      sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true),
      sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
      sheetContent = {
        RequestHistoryDetailContent(response, 0.dp)
      }
    ) {
      RequestHistoryList(listOf(response))
    }
  }
}

