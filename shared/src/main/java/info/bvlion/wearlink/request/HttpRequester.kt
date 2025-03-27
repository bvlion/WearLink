package info.bvlion.wearlink.request

import info.bvlion.wearlink.data.Constant
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.data.ResponseParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http.HttpMethod
import org.json.JSONObject
import java.util.Date

class HttpRequester {
  private val client = OkHttpClient()

  suspend fun execute(
    params: RequestParams,
    isMobile: Boolean = true
  ): ResponseParams = withContext(Dispatchers.IO) {
    val start = System.currentTimeMillis()

    val request = Request.Builder()
      .url(
        if (HttpMethod.permitsRequestBody(params.method.toString()) || params.parameters.isEmpty()) {
          params.url
        } else {
          "${params.url}?${params.parameters}"
        }
      )
      .method(
        params.method.toString(),
        if (HttpMethod.permitsRequestBody(params.method.toString())) {
          params.parameters.toByteArray().let {
            it.toRequestBody(
              if (params.bodyType == Constant.BodyType.JSON) {
                "application/json"
              } else {
                "application/x-www-form-urlencoded"
              }.toMediaType(),
              0,
              it.size
            )
          }
        } else null
      )
      .apply {
        if (params.headers.isNotEmpty()) {
          params.headers.split("\n").associate {
            it.split(":").let { header ->
              if (header.size != 2) {
                "" to ""
              } else {
                header[0].trim() to header[1].trim()
              }
            }
          }
            .filter { it.key.isNotEmpty() && it.value.isNotEmpty() }
            .let { headers(it.toHeaders()) }
        }
      }
      .build()
    client.newCall(request).execute().use { res ->
      val headerJson = JSONObject()
      res.headers.forEach {
        headerJson.put(it.first, it.second)
      }

      return@withContext ResponseParams(
        params.title,
        res.code,
        System.currentTimeMillis() - start,
        headerJson.toString(),
        res.body?.string() ?: "",
        Date().time,
        isMobile
      )
    }
  }
}