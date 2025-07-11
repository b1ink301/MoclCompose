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

#Markwon
-keep class io.noties.markwon.** {*;}
-keep class org.commonmark.** {*;}
-keepnames class io.noties.markwon.** {*;}
-keepnames class org.commonmark.** {*;}
-dontwarn org.commonmark.ext.gfm.strikethrough.Strikethrough

-keep class com.caverock.** { *; }
-dontwarn com.caverock.androidsvg.**

-keep public class pl.droidsonroids.gif.GifIOException{<init>(int, java.lang.String);}

-keep public class androidx.compose.*.**{*;}

-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl

-keep public class * implements com.bumptech.glide.module.GlideModule

-dontwarn java.lang.invoke.StringConcatFactory

-keep class * extends androidx.lifecycle.ViewModel

# Keep Parcelable Classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}