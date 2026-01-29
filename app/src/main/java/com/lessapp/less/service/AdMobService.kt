package com.lessapp.less.service

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class AdResult(
    val ok: Boolean,
    val reason: String? = null
)

object AdMobService {

    // Set to false when app is published
    private const val TEST_MODE = true

    private const val PRODUCTION_REWARDED_UNIT_ID = "ca-app-pub-6402656127562717/5765432109"
    private const val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Android test ID

    private val rewardedUnitId: String
        get() = if (TEST_MODE) TEST_REWARDED_UNIT_ID else PRODUCTION_REWARDED_UNIT_ID

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun initialize(context: Context) {
        MobileAds.initialize(context) { }
    }

    suspend fun loadRewardedAd(context: Context): Boolean {
        if (isLoading) return false
        if (rewardedAd != null) return true

        isLoading = true

        return suspendCancellableCoroutine { continuation ->
            val adRequest = AdRequest.Builder().build()

            RewardedAd.load(context, rewardedUnitId, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    rewardedAd = null
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    isLoading = false
                    rewardedAd = ad
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }
            })
        }
    }

    suspend fun showRewardedVideo(activity: Activity): AdResult {
        // Load ad if not ready
        if (rewardedAd == null) {
            val loaded = loadRewardedAd(activity)
            if (!loaded) {
                return AdResult(ok = false, reason = "not_loaded")
            }
        }

        val ad = rewardedAd ?: return AdResult(ok = false, reason = "not_ready")

        return suspendCancellableCoroutine { continuation ->
            var rewarded = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    if (continuation.isActive) {
                        if (rewarded) {
                            continuation.resume(AdResult(ok = true))
                        } else {
                            continuation.resume(AdResult(ok = false, reason = "closed"))
                        }
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    if (continuation.isActive) {
                        continuation.resume(AdResult(ok = false, reason = error.message))
                    }
                }
            }

            ad.show(activity) { _ ->
                rewarded = true
            }
        }
    }
}
