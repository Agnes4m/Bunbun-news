# Add project specific ProGuard rules here.
# https://developer.android.com/studio/build/shrink-code

# 保留行号便于 crash 报告
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Rome (已移除)
# -keep class com.rometools.** { *; }
# -dontwarn com.rometools.**

# RSS-Parser
-keep class com.prof18.rssparser.** { *; }

# OkHttp/Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod

# Kotlinx Coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
