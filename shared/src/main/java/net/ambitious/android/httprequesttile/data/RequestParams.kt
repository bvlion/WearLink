package net.ambitious.android.httprequesttile.data

import org.json.JSONArray
import org.json.JSONObject

data class RequestParams(
  val title: String,
  val url: String,
  val method: Constant.HttpMethod,
  val bodyType: Constant.BodyType,
  val headers: String = "",
  val parameters: String = "",
  val watchSync: Boolean = false
) {
  fun toJsonString(): String = JSONObject().apply {
    put(TITLE, title)
    put(URL, url)
    put(METHOD, method.name)
    put(BODY_TYPE, bodyType.name)
    put(HEADERS, headers)
    put(PARAMETERS, parameters)
    put(WATCH_SYNC, watchSync)
  }.toString()

  companion object {
    private const val TITLE = "title"
    private const val URL = "url"
    private const val METHOD = "method"
    private const val BODY_TYPE = "bodyType"
    private const val HEADERS = "headers"
    private const val PARAMETERS = "parameters"
    private const val WATCH_SYNC = "watchSync"

    fun String.parseRequestParams(): List<RequestParams> {
      val list = mutableListOf<RequestParams>()
      val jsonArray = JSONArray(this)
      for (i in 0 until jsonArray.length()) {
        JSONObject(jsonArray[i].toString()).let {
          RequestParams(
            it.getString(TITLE),
            it.getString(URL),
            Constant.HttpMethod.valueOf(it.getString(METHOD)),
            Constant.BodyType.valueOf(it.getString(BODY_TYPE)),
            it.getString(HEADERS),
            it.getString(PARAMETERS),
            it.getBoolean(WATCH_SYNC)
          )
        }.let(list::add)
      }
      return list
    }
  }
}