package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback

/**
 * Centered memory-safe AdMob Interstitial Ads helper.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2424129289119816/4019403537"
    
    private var mInterstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    /**
     * Initializes the AdMob SDK and automatically preloads the first ad.
     */
    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds initialized successfully: $status")
                preloadInterstitial(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds: ${e.message}", e)
        }
    }

    /**
     * Caches and preloads the Interstitial Ad in the background.
     */
    fun preloadInterstitial(context: Context) {
        if (mInterstitialAd != null || isAdLoading) {
            return
        }
        
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        
        try {
            InterstitialAd.load(
                context.applicationContext,
                INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "Interstitial ad loaded successfully.")
                        mInterstitialAd = interstitialAd
                        isAdLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                        mInterstitialAd = null
                        isAdLoading = false
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading Interstitial ad: ${e.message}", e)
            isAdLoading = false
        }
    }

    /**
     * Safely displays the cached Interstitial ad or executes the UI transition fallback action.
     */
    fun showInterstitial(context: Context, onAdDismissedOrFailed: () -> Unit) {
        val activity = context.findActivity()
        if (activity != null) {
            showInterstitial(activity, onAdDismissedOrFailed)
        } else {
            Log.e(TAG, "Context does not contain an Activity. Executing fallback directly.")
            preloadInterstitial(context)
            onAdDismissedOrFailed()
        }
    }

    /**
     * Safely displays the cached Interstitial ad or executes the UI transition fallback action.
     */
    fun showInterstitial(activity: Activity, onAdDismissedOrFailed: () -> Unit) {
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    mInterstitialAd = null
                    // Preload immediately for the next transition
                    preloadInterstitial(activity)
                    onAdDismissedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    mInterstitialAd = null
                    preloadInterstitial(activity)
                    onAdDismissedOrFailed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed full screen content.")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Ad was not loaded yet. Executing fallback directly.")
            preloadInterstitial(activity)
            onAdDismissedOrFailed()
        }
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}
