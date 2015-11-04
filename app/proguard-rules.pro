# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in S:\SDK\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Disable logging
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int e(...);
}
#
# Retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-keep class com.slx.funstream.rest.model** { *; }
-keep class com.slx.funstream.model** { *; }
-dontwarn com.slx.funstream.auth.UserStore

#
# okhttp
-dontwarn com.squareup.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
#-keep class com.squareup.** { *; }



# Butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Okio
-dontwarn okio.**

# SearchView fix
-keep class android.support.v7.widget.SearchView { *; }

# JWT
-dontwarn org.spongycastle.**
-dontwarn org.bouncycastle.**
-keep class org.spongycastle.** { *; }
-keep class org.bouncycastle.** { *; }

# Retrolambda
-dontwarn java.lang.invoke.*

# Rx
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
   long producerNode;
   long consumerNode;
}
