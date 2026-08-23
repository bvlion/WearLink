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

    fun List<RequestParams>.moveById(id: String, offset: Int): List<RequestParams> {
      val currentIndex = indexOfFirst { it.id == id }
      if (currentIndex < 0) return this
      val targetIndex = (currentIndex + offset).coerceIn(0, lastIndex)
      if (targetIndex == currentIndex) return this
      return toMutableList().apply {
        add(targetIndex, removeAt(currentIndex))
      }
    }

    fun List<RequestParams>.reorderByIds(ids: List<String>): List<RequestParams> =
      ids.mapNotNull { id -> findById(id) } + filterNot { it.id in ids }

    fun List<RequestParams>.upsertById(request: RequestParams): List<RequestParams> {
      val cleared = if (request.watchfaceShortcut) {
        map { it.copy(watchfaceShortcut = false) }
      } else {
        this
      }
      val index = cleared.indexOfFirst { it.id == request.id }
      return if (index >= 0) {
        cleared.toMutableList().apply { set(index, request) }
      } else {
        listOf(request) + cleared
      }
    }

    fun List<RequestParams>.removeById(id: String): List<RequestParams> = filterNot { it.id == id }

    fun List<RequestParams>.filterByIds(ids: Set<String>): List<RequestParams> = filter { it.id in ids }

    // 既存Requestを残したまま追加するため、端末固有の状態(watchSync / watchfaceShortcut)は持ち込まず、
    // IDが衝突する場合だけ新しいUUIDを発行する。import対象内の順序はJSON配列順のまま維持する。
    fun List<RequestParams>.mergeAdditiveImport(imported: List<RequestParams>): List<RequestParams> {
      val usedIds = map { it.id }.toMutableSet()
      val sanitizedImported = imported.map { request ->
        val sanitized = request.copy(watchSync = false, watchfaceShortcut = false)
        if (usedIds.add(sanitized.id)) {
          sanitized
        } else {
          var newId = UUID.randomUUID().toString()
          while (!usedIds.add(newId)) {
            newId = UUID.randomUUID().toString()
          }
          sanitized.copy(id = newId)
        }
      }
      return sanitizedImported + this
    }

    fun List<RequestParams>.isWatchSyncChangeAllowed(request: RequestParams, maxSyncCount: Int): Boolean {
      val wasSynced = findById(request.id)?.watchSync == true
      if (!request.watchSync || wasSynced) return true
      return count { it.id != request.id && it.watchSync } < maxSyncCount
    }

    fun List<RequestParams>.deduplicateIds(): List<RequestParams> {
      val seenIds = mutableSetOf<String>()
      return map { request ->
        if (seenIds.add(request.id)) {
          request
        } else {
          request.copy(id = UUID.randomUUID().toString())
        }
      }
    }

    fun String.normalizeRequestParamsJson(): String = if (isBlank()) EMPTY_JSON_ARRAY else this

    fun String.needsRequestIdMigration(): Boolean {
      val array = JSONArray(normalizeRequestParamsJson())
      val seenIds = mutableSetOf<String>()
      for (i in 0 until array.length()) {
        val id = JSONObject(array[i].toString()).optString(ID, "")
        if (id.isBlank() || !seenIds.add(id)) {
          return true
        }
      }
      return false
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
          it.optString(ID, "").ifBlank { UUID.randomUUID().toString() }
        )
      }
  }
}
