package net.ambitious.android.httprequesttile.request

import kotlinx.coroutines.runBlocking
import net.ambitious.android.httprequesttile.data.Constant
import net.ambitious.android.httprequesttile.data.RequestParams
import net.ambitious.android.httprequesttile.util.TestUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpRequesterPostTest {

  private val requester = HttpRequester()

  @Test
  fun executePostFormTest() {
    val title = "test_post_form_anything"
    val parameterKey1 = "parameterKey1"
    val parameterValue1 = "parameterValue1"
    val parameterKey2 = "parameterKey2"
    val parameterValue2 = "parameterValue2"

    val actual = runBlocking {
      requester.execute(RequestParams(
        title,
        "http://localhost/anything",
        Constant.HttpMethod.POST,
        Constant.BodyType.FORM_PARAMS,
        parameters = "$parameterKey1=$parameterValue1&$parameterKey2=$parameterValue2"
      ))
    }
    assertEquals(200, actual.responseCode)
    assertEquals(title, actual.title)
    assertEquals(parameterValue1, TestUtil.getFormBody(actual.body, parameterKey1))
    assertEquals(parameterValue2, TestUtil.getFormBody(actual.body, parameterKey2))
    assertEquals(Constant.HttpMethod.POST.name, TestUtil.getMethodBody(actual.body))
  }

  @Test
  fun executePostJsonTest() {
    val title = "test_post_json_anything"
    val parameterKey1 = "parameterKey1"
    val parameterValue1 = "parameterValue1"
    val parameterKey2 = "parameterKey2"
    val parameterValue2 = "parameterValue2"

    val actual = runBlocking {
      requester.execute(RequestParams(
        title,
        "http://localhost/anything",
        Constant.HttpMethod.POST,
        Constant.BodyType.JSON,
        parameters = "{\"$parameterKey1\":\"$parameterValue1\",\"$parameterKey2\":\"$parameterValue2\"}"
      ))
    }
    assertEquals(200, actual.responseCode)
    assertEquals(title, actual.title)
    assertEquals(parameterValue1, TestUtil.getJsonBody(actual.body, parameterKey1))
    assertEquals(parameterValue2, TestUtil.getJsonBody(actual.body, parameterKey2))
    assertEquals(Constant.HttpMethod.POST.name, TestUtil.getMethodBody(actual.body))
  }

  @Test
  fun executePostHeaderTest() {
    val title = "test_post_header_anything"
    val headerKey1 = "Header-Key1"
    val headerValue1 = "HeaderValue1"
    val headerKey2 = "Header-Key2"
    val headerValue2 = "HeaderValue2"

    val actual = runBlocking {
      requester.execute(RequestParams(
        title,
        "http://localhost/anything",
        Constant.HttpMethod.POST,
        Constant.BodyType.FORM_PARAMS,
        headers = "$headerKey1:$headerValue1\n$headerKey2:$headerValue2"
      ))
    }
    assertEquals(200, actual.responseCode)
    assertEquals(title, actual.title)
    assertEquals(headerValue1, TestUtil.getHeadersBody(actual.body, headerKey1))
    assertEquals(headerValue2, TestUtil.getHeadersBody(actual.body, headerKey2))
    assertEquals(Constant.HttpMethod.POST.name, TestUtil.getMethodBody(actual.body))
  }

  @Test
  fun executePostHeaderAndFormTest() {
    val title = "test_post_header_and_form_anything"
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
        "http://localhost/anything",
        Constant.HttpMethod.POST,
        Constant.BodyType.FORM_PARAMS,
        headers = "$headerKey1:$headerValue1\n$headerKey2:$headerValue2",
        parameters = "$parameterKey1=$parameterValue1&$parameterKey2=$parameterValue2"
      ))
    }
    assertEquals(200, actual.responseCode)
    assertEquals(title, actual.title)
    assertEquals(headerValue1, TestUtil.getHeadersBody(actual.body, headerKey1))
    assertEquals(headerValue2, TestUtil.getHeadersBody(actual.body, headerKey2))
    assertEquals(parameterValue1, TestUtil.getFormBody(actual.body, parameterKey1))
    assertEquals(parameterValue2, TestUtil.getFormBody(actual.body, parameterKey2))
    assertEquals(Constant.HttpMethod.POST.name, TestUtil.getMethodBody(actual.body))
  }


  @Test
  fun executePostHeaderAndJsonTest() {
    val title = "test_post_header_and_form_anything"
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
        "http://localhost/anything",
        Constant.HttpMethod.POST,
        Constant.BodyType.JSON,
        headers = "$headerKey1:$headerValue1\n$headerKey2:$headerValue2",
        parameters = "{\"$parameterKey1\":\"$parameterValue1\",\"$parameterKey2\":\"$parameterValue2\"}"
      ))
    }
    assertEquals(200, actual.responseCode)
    assertEquals(title, actual.title)
    assertEquals(headerValue1, TestUtil.getHeadersBody(actual.body, headerKey1))
    assertEquals(headerValue2, TestUtil.getHeadersBody(actual.body, headerKey2))
    assertEquals(parameterValue1, TestUtil.getJsonBody(actual.body, parameterKey1))
    assertEquals(parameterValue2, TestUtil.getJsonBody(actual.body, parameterKey2))
    assertEquals(Constant.HttpMethod.POST.name, TestUtil.getMethodBody(actual.body))
  }

  @Test
  fun executePostStatusCodeTest() {
    val title = "test_post_status_code"
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
          "http://localhost/status/$it",
          Constant.HttpMethod.POST,
          Constant.BodyType.FORM_PARAMS,
        ))
      }
      assertEquals(it, actual.responseCode)
      assertEquals(title, actual.title)
    }
  }
}