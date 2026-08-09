package info.bvlion.wearlink.request

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * ローカルネットワークアクセス権限が不要、または既に許可済みであればtrueを返す。
 * Android 17未満では権限自体が存在しないため常にtrueを返す。
 */
fun Context.hasLocalNetworkAccessPermission(): Boolean =
  Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN ||
    ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_LOCAL_NETWORK
    ) == PackageManager.PERMISSION_GRANTED
