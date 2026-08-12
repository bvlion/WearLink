package info.bvlion.wearlink.request

import info.bvlion.wearlink.data.Constant
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.shared.R
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestExecutionTest {
  private val requester = HttpRequester()

  // OkHttpがRequest構築時点で例外を投げる不正な値。DNS解決や通信待ちに依存せず決定的に失敗させる。
  private val invalidRequest = RequestParams(
    "invalid_url_test",
    "invalid-url",
    Constant.HttpMethod.GET,
    Constant.BodyType.QUERY
  )

  private val getString: (Int) -> String = {
    when (it) {
      R.string.request_error -> "通信エラーが発生しました"
      R.string.local_network_permission_guidance -> "「付近のデバイス」を許可してください"
      else -> ""
    }
  }

  @Test
  fun returnsErrorResponseOnException() = runBlocking {
    val actual = executeRequest(
      requester,
      invalidRequest,
      isMobile = true,
      hasLocalNetworkPermission = true,
      getString = getString
    )

    assertEquals(-1, actual.responseCode)
    assertEquals(invalidRequest.title, actual.title)
    assertTrue(actual.isMobile)
    assertTrue(actual.body.contains(getString(R.string.request_error)))
  }

  @Test
  fun preservesIsMobileFalseForWear() = runBlocking {
    val actual = executeRequest(
      requester,
      invalidRequest,
      isMobile = false,
      hasLocalNetworkPermission = true,
      getString = getString
    )

    assertFalse(actual.isMobile)
  }

  @Test
  fun doesNotAppendGuidanceWhenPermissionGranted() = runBlocking {
    val actual = executeRequest(
      requester,
      invalidRequest,
      isMobile = true,
      hasLocalNetworkPermission = true,
      getString = getString
    )

    assertFalse(actual.body.contains(getString(R.string.local_network_permission_guidance)))
  }

  @Test
  fun appendsGuidanceWhenLocalNetworkPermissionMissing() = runBlocking {
    val actual = executeRequest(
      requester,
      invalidRequest,
      isMobile = true,
      hasLocalNetworkPermission = false,
      getString = getString
    )

    assertTrue(actual.body.contains(getString(R.string.local_network_permission_guidance)))
  }
}
