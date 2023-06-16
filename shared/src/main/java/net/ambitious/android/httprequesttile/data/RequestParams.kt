package net.ambitious.android.httprequesttile.data

import com.google.android.gms.wearable.DataMap
import org.json.JSONObject

data class RequestParams(
  val title: String,
  val url: String,
  val method: Constant.HttpMethod,
  val bodyType: Constant.BodyType,
  val headers: String = "",
  val parameters: String = ""
) {
  fun toJsonString(): String = JSONObject().apply {
    put(TITLE, title)
    put(URL, url)
    put(METHOD, method.name)
    put(BODY_TYPE, bodyType.name)
    put(HEADERS, headers)
    put(PARAMETERS, parameters)
  }.toString()

  companion object {
    const val REQUEST_PARAMS_URI = "/request_params"
    const val REQUEST_PARAMS_LIST_KEY = "request_params_list"

    const val TITLE = "title"
    const val URL = "url"
    const val METHOD = "method"
    const val BODY_TYPE = "bodyType"
    const val HEADERS = "headers"
    const val PARAMETERS = "parameters"
  }
}
