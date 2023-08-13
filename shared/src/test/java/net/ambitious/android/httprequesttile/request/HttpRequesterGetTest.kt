package net.ambitious.android.httprequesttile.request

import kotlinx.coroutines.runBlocking
import net.ambitious.android.httprequesttile.data.Constant
import net.ambitious.android.httprequesttile.data.RequestParams
import net.ambitious.android.httprequesttile.util.TestUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpRequesterGetTest {

  private val requester = HttpRequester()

  @Test
  fun executeGetQueryTest() {
    val title = "test_get_query_anything"
    val parameterKey1 = "parameterKey1"
    val parameterValue1 = "parameterValue1"
    val parameterKey2 = "parameterKey2"
    val parameterValue2 = "parameterValue2"

    val actual = runBlocking {
      requester.execute(RequestParams(
        title,
        "${TestUtil.getTestUrl()}/anything",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
        parameters = "$parameterKey1=$parameterValue1&$parameterKey2=$parameterValue2"
      ))
    }
    assertEquals(200, actual.responseCode)
    assertEquals(title, actual.title)
    assertEquals(parameterValue1, TestUtil.getArgsBody(actual.body, parameterKey1))
    assertEquals(parameterValue2, TestUtil.getArgsBody(actual.body, parameterKey2))
    assertEquals(Constant.HttpMethod.GET.name, TestUtil.getMethodBody(actual.body))
  }

  @Test
  fun executeGetHeaderTest() {
    val title = "test_get_header_anything"
    val headerKey1 = "Header-Key1"
    val headerValue1 = "HeaderValue1"
    val headerKey2 = "Header-Key2"
    val headerValue2 = "HeaderValue2"

    val actual = runBlocking {
      requester.execute(RequestParams(
        title,
        "${TestUtil.getTestUrl()}/anything",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
        headers = "$headerKey1:$headerValue1\n$headerKey2:$headerValue2"
      ))
    }
    assertEquals(200, actual.responseCode)
    assertEquals(title, actual.title)
    assertEquals(headerValue1, TestUtil.getHeadersBody(actual.body, headerKey1))
    assertEquals(headerValue2, TestUtil.getHeadersBody(actual.body, headerKey2))
    assertEquals(Constant.HttpMethod.GET.name, TestUtil.getMethodBody(actual.body))
  }

  @Test
  fun executeGetHeaderAndQueryTest() {
    val title = "test_get_header_and_query_anything"
    val headerKey1 = "Header-Key1"
    val headerValue1 = "HeaderValue1"
    val headerKey2 = "Header-Key2"
    val headerValue2 = "HeaderValue2"
    val parameterKey1 = "parameterKey1"
    val parameterValue1 = "parameterValue1"
    val parameterKey2 = "parameterKey2"
    val parameterValue2 = "parameterValue2"

    val actual = runBlocking {
      requester.execute(RequestParams(
        title,
        "${TestUtil.getTestUrl()}/anything",
        Constant.HttpMethod.GET,
        Constant.BodyType.QUERY,
        headers = "$headerKey1:$headerValue1\n$headerKey2:$headerValue2",
        parameters = "$parameterKey1=$parameterValue1&$parameterKey2=$parameterValue2"
      ))
    }
    assertEquals(200, actual.responseCode)
    assertEquals(title, actual.title)
    assertEquals(headerValue1, TestUtil.getHeadersBody(actual.body, headerKey1))
    assertEquals(headerValue2, TestUtil.getHeadersBody(actual.body, headerKey2))
    assertEquals(parameterValue1, TestUtil.getArgsBody(actual.body, parameterKey1))
    assertEquals(parameterValue2, TestUtil.getArgsBody(actual.body, parameterKey2))
    assertEquals(Constant.HttpMethod.GET.name, TestUtil.getMethodBody(actual.body))
  }

  @Test
  fun executeGetStatusCodeTest() {
    val title = "test_get_status_code"
    val statuses = listOf(
      200,
      201,
      400,
      401,
      403,
      404,
      500,
      503
    )

    statuses.forEach {
      val actual = runBlocking {
        requester.execute(RequestParams(
          title,
          "${TestUtil.getTestUrl()}/status/$it",
          Constant.HttpMethod.GET,
          Constant.BodyType.QUERY,
        ))
      }
      assertEquals(it, actual.responseCode)
      assertEquals(title, actual.title)
    }
  }
}