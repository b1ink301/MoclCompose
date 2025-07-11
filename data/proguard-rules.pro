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

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class kr.b1ink.data.site.** { <fields>; }
-keep class kr.b1ink.data.db.dao.** { <fields>; }
-keep class kr.b1ink.data.db.entity.** { <fields>; }
-keep class kr.b1ink.data.site.dto.** { <fields>; }
-keep class kr.b1ink.data.site.base.** { <fields>; }
-keep class kr.b1ink.data.site.api.** { <fields>; }
-keep class kr.b1ink.data.di.coroutine.** { <fields>; }

-keep class kr.b1ink.data.site.navercafe.** { <fields>; }
-keepclassmembers class kr.b1ink.data.site.navercafe.** { <fields>; }

-keep class kr.b1ink.data.site.meeco.** { <fields>; }
-keepclassmembers class kr.b1ink.data.site.meeco.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

##---------------End: proguard configuration for Gson  ----------

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-keep public class org.jsoup.** {
	public *;
}

-dontwarn javax.**
-dontwarn io.realm.**

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keeppackagenames org.jsoup.nodes

-dontwarn java.lang.invoke.StringConcatFactory

-dontwarn javax.inject.**
# Keep file R and Manifest
-keep class **.R
-keep class **.R$* {*;}
-keep class **.BuildConfig {*;}
-keep class **.Manifest {*;}

-keep class androidx.constraintlayout.motion.widget.KeyAttributes { *; }

# Keep Dependency Injection Framework related classes and methods
-keep class dagger.** { *; }
-keep class *Hilt_* {*;}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class javax.annotation.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel

# Keep Parcelable Classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Menjaga kelas-kelas yang diperlukan untuk penanganan SSL/TLS
-keep class okhttp3.internal.platform.ConscryptPlatform {*;}
-keep class okhttp3.internal.platform.OpenJSSEPlatform {*;}
-keep class org.bouncycastle.** {*;}
-keep class org.conscrypt.** {*;}
-keep class org.openjsse.** {*;}


# Keep classes required by OpenJSSE
-keep class sun.security.x509.** { *; }
-keep class sun.util.logging.** { *; }

# Keep classes generated by Hilt
-keep class com.example.projectone.*Hilt* {
    *;
}
-keepclasseswithmembernames class com.example.projectone.*Hilt* {
    *;
}
-keepclasseswithmembernames class com.example.projectone.*Hilt_* {
    *;
}

-keepattributes *Annotation*
-keep @dagger.hilt.annotation.* class * { *; }

-keepclassmembers class * {
    @dagger.hilt.* *;
}
-keepclassmembers class * {
    @javax.inject.* *;
}
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}
-keep class kotlin.Metadata { *; }

# Keep Hilt generated classes
-keep class com.example.projectone.Hilt_* {*;}
-keep,allowobfuscation,allowshrinking @dagger.hilt.EntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.EarlyEntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.internal.ComponentEntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.internal.GeneratedEntryPoint class *

-keep class kotlin.**
-keep class javax.** { *; }

# Keep Hilt generated classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# Understand the @Keep support annotation.
-keep class androidx.annotation.Keep

-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

-keep class **.R

-keepnames class kotlinx.** { *; }
-keep class kotlinx.coroutines.**
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.ContinuationInterceptor { *; }
-keepclassmembers class kotlinx.coroutines.internal.DispatchedContinuation { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 함수형 인터페이스 (SAM) 변환을 위한 규칙 (만약 람다를 많이 사용한다면)
-keepclassmembers class ** {
    ** lambda*(...);
}
-keepclassmembers class ** {
    ** access$*(...);
}

-keep class okhttp3.internal.publicsuffix.** { *; }
-keepnames class okhttp3.internal.publicsuffix.** { *; }
-keeppackagenames okhttp3.**
-keep class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keeppackagenames okhttp3.internal.publicsuffix.*
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

