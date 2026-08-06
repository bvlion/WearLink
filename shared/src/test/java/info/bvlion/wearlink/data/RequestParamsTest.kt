package info.bvlion.wearlink.data

import info.bvlion.wearlink.data.RequestParams.Companion.normalizeRequestParamsJson
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.toRequestParamsJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestParamsTest {
  private val watchSyncRequest = RequestParams(
    title = "watch sync request",
    url = "https://example.com/sync",
    method = Constant.HttpMethod.GET,
    bodyType = Constant.BodyType.QUERY,
    watchSync = true
  )
  private val watchfaceShortcutRequest = RequestParams(
    title = "watchface shortcut request",
    url = "https://example.com/shortcut",
    method = Constant.HttpMethod.POST,
    bodyType = Constant.BodyType.JSON,
    watchfaceShortcut = true
  )

  @Test
  fun toRequestParamsJsonEmptyListTest() {
    assertEquals("[]", emptyList<RequestParams>().toRequestParamsJson())
  }

  @Test
  fun parseRequestParamsEmptyJsonArrayTest() {
    assertTrue("[]".parseRequestParams().isEmpty())
  }

  @Test
  fun toRequestParamsJsonRoundTripTest() {
    val requests = listOf(watchSyncRequest, watchfaceShortcutRequest)

    val actual = requests.toRequestParamsJson().parseRequestParams()

    assertEquals(requests, actual)
  }

  @Test
  fun normalizeRequestParamsJsonBlankTest() {
    assertEquals("[]", "".normalizeRequestParamsJson())
    assertEquals("[]", "   ".normalizeRequestParamsJson())
  }

  @Test
  fun normalizeRequestParamsJsonBlankParsesWithoutExceptionTest() {
    assertTrue("".normalizeRequestParamsJson().parseRequestParams().isEmpty())
  }

  @Test
  fun normalizeRequestParamsJsonNonBlankUnchangedTest() {
    val json = listOf(watchSyncRequest).toRequestParamsJson()

    assertEquals(json, json.normalizeRequestParamsJson())
  }
}
