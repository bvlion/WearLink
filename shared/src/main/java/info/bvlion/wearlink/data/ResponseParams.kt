package info.bvlion.wearlink.data

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

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

  // sendDateTimeだけではmobile/Wear OSで独立生成された値が衝突し得るため、内容全体からのdigestをsaveable selection keyとして使う
  fun selectionKey(): String {
    val digestSource = listOf(sendDateTime, title, responseCode, execTime, header, body, isMobile)
      .joinToString(RESPONSE_KEY_SEPARATOR)
    val digest = MessageDigest.getInstance("SHA-256").digest(digestSource.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }

  companion object {
    private const val TITLE = "title"
    private const val RESPONSE_CODE = "responseCode"
    private const val EXEC_TIME = "execTime"
    private const val HEADER = "header"
    private const val BODY = "body"
    private const val SEND_DATE_TIME = "sendDate"
    private const val IS_MOBILE = "isMobile"
    private const val RESPONSE_KEY_SEPARATOR = "-"

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

    fun List<ResponseParams>.findBySelectionKey(key: String?): ResponseParams? =
      key?.let { target -> find { it.selectionKey() == target } }
  }
}
