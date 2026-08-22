package info.bvlion.wearlink.data

import info.bvlion.wearlink.data.ResponseParams.Companion.findBySendDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResponseParamsTest {
  private fun response(sendDateTime: Long, title: String = "response-$sendDateTime") = ResponseParams(
    title = title,
    responseCode = 200,
    execTime = 10,
    header = "",
    body = "",
    sendDateTime = sendDateTime,
    isMobile = true,
  )

  @Test
  fun findBySendDateTimeReturnsMatchingResponseTest() {
    val first = response(100)
    val second = response(200)
    val responses = listOf(first, second)

    assertEquals(second, responses.findBySendDateTime(200))
  }

  @Test
  fun findBySendDateTimeWithNullKeyReturnsNullTest() {
    val responses = listOf(response(100))

    assertNull(responses.findBySendDateTime(null))
  }

  @Test
  fun findBySendDateTimeWithMissingKeyReturnsNullTest() {
    val responses = listOf(response(100), response(200))

    assertNull(responses.findBySendDateTime(999))
  }

  @Test
  fun findBySendDateTimeOnEmptyListReturnsNullTest() {
    assertNull(emptyList<ResponseParams>().findBySendDateTime(100))
  }
}
