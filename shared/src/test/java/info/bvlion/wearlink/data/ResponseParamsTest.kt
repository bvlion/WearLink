package info.bvlion.wearlink.data

import info.bvlion.wearlink.data.ResponseParams.Companion.findBySelectionKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResponseParamsTest {
  private fun response(
    sendDateTime: Long,
    title: String = "response-$sendDateTime",
    responseCode: Int = 200,
    execTime: Long = 10,
    header: String = "",
    body: String = "",
    isMobile: Boolean = true,
  ) = ResponseParams(
    title = title,
    responseCode = responseCode,
    execTime = execTime,
    header = header,
    body = body,
    sendDateTime = sendDateTime,
    isMobile = isMobile,
  )

  @Test
  fun findBySelectionKeyReturnsMatchingResponseTest() {
    val first = response(100)
    val second = response(200)
    val responses = listOf(first, second)

    assertEquals(second, responses.findBySelectionKey(second.selectionKey()))
  }

  @Test
  fun findBySelectionKeyWithNullKeyReturnsNullTest() {
    val responses = listOf(response(100))

    assertNull(responses.findBySelectionKey(null))
  }

  @Test
  fun findBySelectionKeyWithMissingKeyReturnsNullTest() {
    val responses = listOf(response(100), response(200))

    assertNull(responses.findBySelectionKey(response(999).selectionKey()))
  }

  @Test
  fun findBySelectionKeyOnEmptyListReturnsNullTest() {
    assertNull(emptyList<ResponseParams>().findBySelectionKey(response(100).selectionKey()))
  }

  @Test
  fun findBySelectionKeyDistinguishesResponsesWithSameSendDateTimeTest() {
    val mobileResponse = response(sendDateTime = 100, title = "mobile", isMobile = true)
    val wearResponse = response(sendDateTime = 100, title = "wear", isMobile = false)
    val responses = listOf(mobileResponse, wearResponse)

    assertEquals(mobileResponse, responses.findBySelectionKey(mobileResponse.selectionKey()))
    assertEquals(wearResponse, responses.findBySelectionKey(wearResponse.selectionKey()))
  }

  @Test
  fun selectionKeyIsStableForEqualContentTest() {
    val first = response(100)
    val second = response(100)

    assertEquals(first.selectionKey(), second.selectionKey())
  }

  @Test
  fun selectionKeyDiffersForDifferentContentTest() {
    val first = response(100, title = "first")
    val second = response(100, title = "second")

    assertNotEquals(first.selectionKey(), second.selectionKey())
  }
}
