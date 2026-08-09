package info.bvlion.wearlink.request

import info.bvlion.wearlink.data.Constant
import info.bvlion.wearlink.data.RequestParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class HttpRequester {
  private val client = OkHttpClient()

  suspend fun execute(params: RequestParams): HttpResult = withContext(Dispatchers.IO) {
    val isRequestBodyPermitted = params.method != Constant.HttpMethod.GET

    val request = Request.Builder()
      .url(
        if (isRequestBodyPermitted || params.parameters.isEmpty()) {
          params.url
        } else {
          "${params.url}?${params.parameters}"
        }
      )
      .method(
        params.method.toString(),
        if (isRequestBodyPermitted) {
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

      return@withContext HttpResult(
        res.code,
        headerJson.toString(),
        res.body?.string() ?: ""
      )
    }
  }
}

data class HttpResult(
  val responseCode: Int,
  val header: String,
  val body: String
)
