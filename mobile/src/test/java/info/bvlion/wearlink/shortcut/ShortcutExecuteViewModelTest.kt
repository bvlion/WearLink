package info.bvlion.wearlink.shortcut

import info.bvlion.wearlink.data.Constant
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.toRequestParamsJson
import info.bvlion.wearlink.data.ResponseParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutExecuteViewModelTest {
  private val request = RequestParams(
    title = "shortcut request",
    url = "https://example.com/shortcut",
    method = Constant.HttpMethod.GET,
    bodyType = Constant.BodyType.QUERY,
  )

  private fun response(responseCode: Int) =
    ResponseParams(request.title, responseCode, 10L, "", "", 0L, true)

  @Test
  fun resolveRequestFindsMatchingRequestByIdTest() {
    val savedJson = listOf(request).toRequestParamsJson()

    assertEquals(request, resolveRequest(request.id, savedJson))
  }

  @Test
  fun resolveRequestReturnsNullForBlankIdTest() {
    val savedJson = listOf(request).toRequestParamsJson()

    assertNull(resolveRequest("", savedJson))
    assertNull(resolveRequest(null, savedJson))
    assertNull(resolveRequest("   ", savedJson))
  }

  @Test
  fun resolveRequestReturnsNullForUnknownIdTest() {
    val savedJson = listOf(request).toRequestParamsJson()

    assertNull(resolveRequest("unknown-id", savedJson))
  }

  @Test
  fun resolveRequestReturnsNullWhenNoSavedRequestsTest() {
    assertNull(resolveRequest(request.id, null))
    assertNull(resolveRequest(request.id, ""))
  }

  @Test
  fun outcomeForSuccessResponseCodeIsSuccessTest() {
    assertEquals(ShortcutExecuteState.Success(request.title), outcomeFor(response(200)))
  }

  @Test
  fun outcomeForFailureResponseCodeIsFailureTest() {
    assertEquals(ShortcutExecuteState.Failure(request.title), outcomeFor(response(404)))
    assertEquals(ShortcutExecuteState.Failure(request.title), outcomeFor(response(-1)))
  }

  @Test
  fun awaitAtLeastWaitsForMinimumDelayWhenBlockIsFasterTest() = runBlocking {
    val start = System.currentTimeMillis()

    awaitAtLeast(1000L) {
      delay(100)
      response(200)
    }

    val elapsed = System.currentTimeMillis() - start
    assertTrue("expected at least ~1000ms, was ${elapsed}ms", elapsed >= 950)
  }

  @Test
  fun awaitAtLeastReturnsAsSoonAsSlowerBlockCompletesTest() = runBlocking {
    val start = System.currentTimeMillis()

    awaitAtLeast(200L) {
      delay(600)
      response(200)
    }

    val elapsed = System.currentTimeMillis() - start
    assertTrue("expected roughly 600ms, was ${elapsed}ms", elapsed in 550..1200)
  }

  @Test
  fun awaitAtLeastReturnsResultFromBlockTest() = runBlocking {
    val expected = response(200)

    val actual = awaitAtLeast(10L) { expected }

    assertEquals(expected, actual)
  }
}
