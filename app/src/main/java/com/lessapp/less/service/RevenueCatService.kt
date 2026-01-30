package com.lessapp.less.service

import android.app.Application
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchaseParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object RevenueCatService {

    private const val API_KEY = "goog_YOUR_ANDROID_REVENUECAT_KEY" // Replace with Android key from RevenueCat dashboard

    // Product IDs (same as iOS)
    const val TIP_SMALL_ID = "less_tip_small"
    const val TIP_MEDIUM_ID = "less_tip_medium"
    const val TIP_LARGE_ID = "less_tip_large"

    private var isConfigured = false

    fun configure(application: Application) {
        if (isConfigured) return
        if (API_KEY == "goog_YOUR_ANDROID_REVENUECAT_KEY") {
            println("RevenueCat: API key not configured")
            return
        }

        Purchases.logLevel = com.revenuecat.purchases.LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(application, API_KEY).build()
        )
        isConfigured = true
        println("RevenueCat: Configured successfully")
    }

    suspend fun fetchOfferings(): Offerings? {
        if (!isConfigured) return null

        return suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    if (continuation.isActive) {
                        continuation.resume(offerings)
                    }
                }

                override fun onError(error: PurchasesError) {
                    println("RevenueCat: Failed to fetch offerings: ${error.message}")
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            })
        }
    }

    suspend fun purchase(activity: android.app.Activity, pkg: Package): Boolean {
        if (!isConfigured) return false

        return suspendCancellableCoroutine { continuation ->
            val purchaseParams = PurchaseParams.Builder(activity, pkg).build()
            Purchases.sharedInstance.purchase(
                purchaseParams,
                object : PurchaseCallback {
                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                        println("RevenueCat: Purchase successful")
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        if (userCancelled) {
                            println("RevenueCat: Purchase cancelled by user")
                        } else {
                            println("RevenueCat: Purchase error: ${error.message}")
                        }
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                }
            )
        }
    }

    suspend fun restorePurchases(): Boolean {
        if (!isConfigured) return false

        return suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.restorePurchases(object : com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    println("RevenueCat: Restored purchases")
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }

                override fun onError(error: PurchasesError) {
                    println("RevenueCat: Restore failed: ${error.message}")
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            })
        }
    }
}
