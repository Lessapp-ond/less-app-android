# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keep class com.lessapp.less.data.model.** { *; }

# Supabase
-keep class io.github.jan.supabase.** { *; }

# RevenueCat
-keep class com.revenuecat.purchases.** { *; }

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlinx Coroutines
-dontwarn kotlinx.coroutines.**

# SLF4J (used by Ktor)
-dontwarn org.slf4j.**
