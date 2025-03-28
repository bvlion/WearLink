package info.bvlion.wearlink.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.ui.theme.WearLinkTheme
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
    elevation = CardDefaults.cardElevation(2.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(start = 16.dp)) {
        Text(
          text = stringResource(if (responseParams.isMobile) {
            R.string.request_history_from_mobile
          } else {
            R.string.request_history_from_wearlable
          }),
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
          text = stringResource(R.string.request_history_card_response, responseParams.responseCode),
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
  topPadding: Dp = 8.dp,
  bottomPadding: Dp = 0.dp,
  modalBottomSheetShow: (ResponseParams) -> Unit = {},
) {
  if (responses.isEmpty()) {
    Box(
      Modifier
        .fillMaxSize()
        .padding(bottom = 24.dp + bottomPadding),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = stringResource(R.string.request_history_empty),
        fontSize = 13.sp
      )
    }
  } else {
    LazyColumn(Modifier.fillMaxWidth()) {
      itemsIndexed(responses) { index, response ->
        RequestHistory(
          response,
          if (index == 0) topPadding else 0.dp,
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
        text = stringResource(R.string.request_history_bottom_sheet_response_code),
        modifier = Modifier.padding(end = 8.dp),
        fontSize = 14.sp
      )
      Text(responseParams.responseCode.toString())
    }
    Row(Modifier.padding(top = 16.dp)) {
      Text(
        text = stringResource(R.string.request_history_bottom_sheet_exec_time_title),
        modifier = Modifier.padding(end = 8.dp),
        fontSize = 14.sp
      )
      Text(
        stringResource(
          R.string.request_history_bottom_sheet_exec_time_sec,
          NumberFormat.getNumberInstance().format(responseParams.execTime)
        )
      )
    }
    Text(
      text = stringResource(R.string.request_history_bottom_sheet_response_body),
      modifier = Modifier.padding(top = 16.dp),
      fontSize = 14.sp
    )
    Text(
      text = responseParams.body,
      modifier = Modifier.padding(top = 8.dp),
    )
    Text(
      text = stringResource(R.string.request_history_bottom_sheet_response_header),
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

private val dummyResponse = ResponseParams(
  "HogeHoge の Request",
  200,
  45,
  "aaaa",
  "bbbb",
  System.currentTimeMillis(),
  false
)

@Preview(showBackground = true)
@Composable
private fun RequestHistoryListEmptyPreview() {
  WearLinkTheme {
    RequestHistoryList(emptyList())
  }
}

@Preview(showBackground = true)
@Composable
private fun RequestHistoryListHasItemPreview() {
  WearLinkTheme {
    RequestHistoryList(List(3) { dummyResponse })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun RequestHistoryDetailContentPreview() {
  WearLinkTheme {
    val sheetState = rememberStandardBottomSheetState(
      initialValue = SheetValue.Expanded
    )

    ModalBottomSheet(
      onDismissRequest = {},
      sheetState = sheetState,
      shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
      RequestHistoryDetailContent(dummyResponse, 0.dp)
    }
  }
}
