package info.bvlion.wearlink.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Parcelize
data class RequestParams(
  val title: String,
  val url: String,
  val method: Constant.HttpMethod,
  val bodyType: Constant.BodyType,
  val headers: String = "",
  val parameters: String = "",
  val watchSync: Boolean = false,
  val watchfaceShortcut: Boolean = false,
  val id: String = UUID.randomUUID().toString(),
): Parcelable {
  fun toJsonString(): String = JSONObject().apply {
    put(TITLE, title)
    put(URL, url)
    put(METHOD, method.name)
    put(BODY_TYPE, bodyType.name)
    put(HEADERS, headers)
    put(PARAMETERS, parameters)
    put(WATCH_SYNC, watchSync)
    put(WATCH_FACE_SHORTCUT, watchfaceShortcut)
    put(ID, id)
  }.toString()

  companion object {
    private const val TITLE = "title"
    private const val URL = "url"
    private const val METHOD = "method"
    private const val BODY_TYPE = "bodyType"
    private const val HEADERS = "headers"
    private const val PARAMETERS = "parameters"
    private const val WATCH_SYNC = "watchSync"
    private const val WATCH_FACE_SHORTCUT = "watchfaceShortcut"
    private const val ID = "id"
    private const val EMPTY_JSON_ARRAY = "[]"

    fun List<RequestParams>.toRequestParamsJson(): String =
      JSONArray(map { it.toJsonString() }).toString()

    fun List<RequestParams>.findById(id: String): RequestParams? = find { it.id == id }

    fun String.normalizeRequestParamsJson(): String = if (isBlank()) EMPTY_JSON_ARRAY else this

    fun String.needsRequestIdMigration(): Boolean =
      JSONArray(normalizeRequestParamsJson()).let { array ->
        (0 until array.length()).any { i -> !JSONObject(array[i].toString()).has(ID) }
      }

    fun String.parseRequestParams(): List<RequestParams> {
      val list = mutableListOf<RequestParams>()
      val jsonArray = JSONArray(this)
      for (i in 0 until jsonArray.length()) {
        list.add(jsonArray[i].toString().parseRequestParam())
      }
      return list
    }

    fun String.parseRequestParam(): RequestParams =
      JSONObject(this).let {
        RequestParams(
          it.getString(TITLE),
          it.getString(URL),
          Constant.HttpMethod.valueOf(it.getString(METHOD)),
          Constant.BodyType.valueOf(it.getString(BODY_TYPE)),
          it.getString(HEADERS),
          it.getString(PARAMETERS),
          it.getBoolean(WATCH_SYNC),
          it.optBoolean(WATCH_FACE_SHORTCUT),
          if (it.has(ID)) it.getString(ID) else UUID.randomUUID().toString()
        )
      }
  }
}
