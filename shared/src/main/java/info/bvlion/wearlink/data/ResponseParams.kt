package info.bvlion.wearlink.data

import org.json.JSONArray
import org.json.JSONObject

data class ResponseParams(
  val title: String,
  val responseCode: Int,
  val execTime: Long,
  val header: String,
  val body: String,
  val sendDateTime: Long,
  val isMobile: Boolean
) {
  fun toJsonString(): String = JSONObject().apply {
    put(TITLE, title)
    put(RESPONSE_CODE, responseCode)
    put(EXEC_TIME, execTime)
    put(HEADER, header)
    put(BODY, body)
    put(SEND_DATE_TIME, sendDateTime)
    put(IS_MOBILE, isMobile)
  }.toString()

  companion object {
    private const val TITLE = "title"
    private const val RESPONSE_CODE = "responseCode"
    private const val EXEC_TIME = "execTime"
    private const val HEADER = "header"
    private const val BODY = "body"
    private const val SEND_DATE_TIME = "sendDate"
    private const val IS_MOBILE = "isMobile"

    fun String.parseResponseParams(): List<ResponseParams> {
      val list = mutableListOf<ResponseParams>()
      val jsonArray = JSONArray(this)
      for (i in 0 until jsonArray.length()) {
        JSONObject(jsonArray[i].toString()).let {
          ResponseParams(
            it.getString(TITLE),
            it.getInt(RESPONSE_CODE),
            it.getLong(EXEC_TIME),
            it.getString(HEADER),
            it.getString(BODY),
            it.getLong(SEND_DATE_TIME),
            it.getBoolean(IS_MOBILE)
          )
        }.let(list::add)
      }
      return list
    }
  }
}