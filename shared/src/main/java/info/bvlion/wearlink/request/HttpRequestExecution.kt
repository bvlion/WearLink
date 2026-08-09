package info.bvlion.wearlink.request

import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.shared.R
import java.util.Date

/**
 * [HttpRequester.execute]を実行し、例外発生時は[ResponseParams]へ変換して返す。
 * 成功時の計測は[HttpRequester.execute]内部で行われるため、ここでは変更しない。
 * Context・AppDataStoreへは依存せず、エラーメッセージの組み立てに必要な値のみを引数で受け取る。
 *
 * @param hasLocalNetworkPermission ローカルネットワークアクセス権限が不要、または許可済みであればtrue
 * @param getString 文字列リソース取得用のcallback
 */
suspend fun HttpRequester.executeCatching(
  params: RequestParams,
  isMobile: Boolean,
  hasLocalNetworkPermission: Boolean,
  getString: (Int) -> String
): ResponseParams {
  val start = System.currentTimeMillis()
  return try {
    execute(params, isMobile)
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
