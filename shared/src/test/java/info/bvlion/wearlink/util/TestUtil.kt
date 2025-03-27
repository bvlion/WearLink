package info.bvlion.wearlink.util

import org.json.JSONObject

object TestUtil {
  fun getArgsBody(body: String, key: String): String =
    JSONObject(body).getJSONObject("args").getString(key)

  fun getMethodBody(body: String): String =
    JSONObject(body).getString("method")

  fun getHeadersBody(body: String, key: String): String {
    val headers = JSONObject(body).getJSONObject("headers")
    return headers.getString(key)
  }

  fun getFormBody(body: String, key: String): String =
    JSONObject(body).getJSONObject("form").getString(key)

  fun getJsonBody(body: String, key: String): String =
    JSONObject(body).getString("data").let {
      JSONObject(it).getString(key)
    }

  fun getTestUrl() = System.getenv("HOST") ?: "https://httpbin.org"
}