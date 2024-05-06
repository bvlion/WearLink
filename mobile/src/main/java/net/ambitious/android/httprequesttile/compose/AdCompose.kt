package net.ambitious.android.wearnetvoyage.compose

import android.os.Bundle
import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import net.ambitious.android.wearnetvoyage.BuildConfig
import net.ambitious.android.wearnetvoyage.databinding.AdmobBinding
import net.ambitious.android.wearnetvoyage.ui.theme.AppTheme

@Composable
fun NativeAdCompose() {
  val context = LocalContext.current
  val textColor = MaterialTheme.colors.onBackground.toArgb()
  AndroidViewBinding(AdmobBinding::inflate) {
    AdLoader.Builder(context, BuildConfig.ADMOB_NATIVE_KEY)
      .forNativeAd { nativeAd ->
        adImage.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        nativeAd.mediaContent?.let {
          adImage.mediaContent = it
        }
        nativeAdView.mediaView = adImage

        nativeAdView.headlineView = adHeadline.apply {
          text = nativeAd.headline
          setTextColor(textColor)
        }

        nativeAdView.bodyView = adBody.apply {
          text = nativeAd.body
          setTextColor(textColor)
        }

        nativeAdView.advertiserView = advertiser.apply {
          text = if (!nativeAd.advertiser.isNullOrEmpty()) {
            "[広告]・" + nativeAd.advertiser
          } else {
            "[広告]"
          }
          setTextColor(textColor)
        }

        nativeAdView.setNativeAd(nativeAd)
      }.build().loadAd(
        Bundle().apply {
          putString("max_ad_content_rating", "G")
        }.let {
          AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, it).build()
        }
      )
  }
}

@Composable
fun DummyAdCompose() {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp)) {
    Box(modifier = Modifier
      .width(164.dp)
      .height(120.dp), contentAlignment = Alignment.Center) {
      Text("画像")
    }
    Column(Modifier.fillMaxWidth().padding(start = 8.dp)) {
      Text("Headlineをここに表示Headlineをここに表示します", fontSize = 12.sp)
      Text("詳細文をここに表示詳細文をここに表示詳細文をここに表示します", fontSize = 14.sp)
      Text("広告アカウント", fontSize = 13.sp)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AdPreview() {
  AppTheme {
    DummyAdCompose()
  }
}
