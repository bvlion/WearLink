package info.bvlion.wearlink.request

import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.shared.R
import java.util.Date

suspend fun executeRequest(
  requester: HttpRequester,
  params: RequestParams,
  isMobile: Boolean,
  hasLocalNetworkPermission: Boolean,
  getString: (Int) -> String
): ResponseParams {
  val start = System.currentTimeMillis()
  return try {
    val result = requester.execute(params)
    ResponseParams(
      params.title,
      result.responseCode,
      System.currentTimeMillis() - start,
      result.header,
      result.body,
      Date().time,
      isMobile
    )
  } catch (e: Exception) {
    val localNetworkPermissionGuidance = if (hasLocalNetworkPermission) {
      ""
    } else {
      "\n${getString(R.string.local_network_permission_guidance)}"
    }
    ResponseParams(
      params.title,
      -1,
      System.currentTimeMillis() - start,
      "",
      "${getString(R.string.request_error)}\n${e.message}" + localNetworkPermissionGuidance,
      Date().time,
      isMobile
    )
  }
}
