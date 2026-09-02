package info.bvlion.wearlink.data

import android.content.Context
import android.content.Intent
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.core.net.toUri
import info.bvlion.wearlink.request.WearMobileConnector
import kotlinx.coroutines.flow.first

object AppConstants {
  const val SYNC_STORE_DATA = "info.bvlion.SYNC_STORE_DATA"

  /** UNKNOWN は「原因を断定できなかった」であり、未接続や未インストールを意味しない */
  enum class PhoneConnectionStatus {
    MOBILE_APP_MISSING,
    PHONE_DISCONNECTED,
    UNKNOWN,
  }

  fun startMobileActivity(
    context: Context,
    url: String = "wearlink://start",
    successProcess: () -> Unit,
    errorProcess: () -> Unit = {}
  ) = startRemoteActivity(context, url, successProcess, errorProcess)

  // wear と mobile は applicationId を共有し Play リスティングも 1 つのため、
  // wear 自身の packageName でスマホ版のストアページが開く
  fun openPhonePlayStore(
    context: Context,
    successProcess: () -> Unit,
    errorProcess: () -> Unit = {}
  ) = startRemoteActivity(
    context,
    "market://details?id=${context.packageName}",
    successProcess,
    errorProcess
  )

  private fun startRemoteActivity(
    context: Context,
    url: String,
    successProcess: () -> Unit,
    errorProcess: () -> Unit
  ) {
    val future = RemoteActivityHelper(context).startRemoteActivity(
      Intent(Intent.ACTION_VIEW)
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setData(url.toUri())
    )
    future.addListener({
      try {
        future.get()
        successProcess()
      } catch (e: Exception) {
        errorProcess()
      }
    }, context.mainExecutor)
  }

  /**
   * 「スマホで開く」が失敗した後に呼び、原因を分類する。
   * CapabilityClient にスマホ版が見えないことだけでは「未インストール」と断定しない。
   * 古い Wear OS では availabilityStatus が常に STATUS_UNKNOWN になるため、その場合も断定しない。
   */
  suspend fun resolvePhoneConnectionStatus(context: Context): PhoneConnectionStatus {
    val connector = WearMobileConnector(context)

    // true = 到達できるのに起動失敗（一時的）、null = 問い合わせ自体が失敗。どちらも断定しない
    if (connector.isMobileAppReachable() != false) return PhoneConnectionStatus.UNKNOWN

    val phoneConnected = connector.isPhoneConnected()
    val remoteStatus = try {
      RemoteActivityHelper(context).availabilityStatus.first()
    } catch (e: Exception) {
      RemoteActivityHelper.STATUS_UNKNOWN
    }

    return when {
      phoneConnected -> PhoneConnectionStatus.MOBILE_APP_MISSING
      remoteStatus == RemoteActivityHelper.STATUS_UNAVAILABLE ||
        remoteStatus == RemoteActivityHelper.STATUS_TEMPORARILY_UNAVAILABLE ->
        PhoneConnectionStatus.PHONE_DISCONNECTED
      remoteStatus == RemoteActivityHelper.STATUS_AVAILABLE -> PhoneConnectionStatus.MOBILE_APP_MISSING
      else -> PhoneConnectionStatus.UNKNOWN
    }
  }
}
