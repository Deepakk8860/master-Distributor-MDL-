# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type
-keepattributes JavascriptInterface
-keepattributes *Annotation*
-optimizations !method/inlining/*
-keep class sqip.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-dontwarn com.android.masterdistributormdl.model.**
-dontwarn com.android.masterdistributormdl.gskDistributor.model.**
-keep class com.android.masterdistributormdl.model.** {*;}
-keep class com.android.masterdistributormdl.gskDistributor.model.** {*;}
-keep class com.pushwoosh.** { *; }
-keep class com.paytm.pgsdk.** { *; }
-keep interface com.paytm.pgsdk.** { *; }
-dontwarn com.pushwoosh.**

