package info.bvlion.wearlink.data

import android.content.Context
import android.content.Intent
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.core.net.toUri
import info.bvlion.wearlink.request.WearMobileConnector
import kotlinx.coroutines.flow.first

object AppConstants {
  const val SYNC_STORE_DATA = "info.bvlion.SYNC_STORE_DATA"

  /** mobile / wear は同一 applicationId で、Play リスティングも 1 つ */
  private const val PLAY_STORE_URL = "market://details?id=net.ambitious.android.wearlink"

  /** 「スマホで開く」失敗時に、原因を分類できた範囲で表す */
  enum class PhoneConnectionStatus {
    /** スマホ版 WearLink が未インストールと判断できる */
    MOBILE_APP_MISSING,

    /** スマホ自体と接続できていないと判断できる */
    PHONE_DISCONNECTED,

    /** 古い Wear OS など、状態だけでは原因を判別できない */
    UNKNOWN,
  }

  fun startMobileActivity(
    context: Context,
    url: String = "wearlink://start",
    successProcess: () -> Unit,
    errorProcess: () -> Unit = {}
  ) = startRemoteActivity(context, url, successProcess, errorProcess)

  /** ペアリング済みスマホの Google Play で WearLink のストアページを開く */
  fun openPhonePlayStore(
    context: Context,
    successProcess: () -> Unit,
    errorProcess: () -> Unit = {}
  ) = startRemoteActivity(context, PLAY_STORE_URL, successProcess, errorProcess)

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
   * CapabilityClient にスマホ版が見えないことだけで「未インストール」と断定せず、
   * スマホ自体の到達可否や RemoteActivityHelper の状態も加味する。
   * 古い Wear OS では availabilityStatus が STATUS_UNKNOWN のため、その場合は断定しない。
   */
  suspend fun resolvePhoneConnectionStatus(context: Context): PhoneConnectionStatus {
    val connector = WearMobileConnector(context)

    // false（問い合わせは成功しスマホ版が見つからなかった）以外は原因を断定しない。
    // true = 到達できているのに起動失敗（一時的）、null = 問い合わせ自体が失敗。
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
