# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
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

# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.


# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-printusage shrinking.outpu


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** d(...);
    public static *** e(...);
}
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
    #下面这句是下面R里面的
    #public static <fields>;
-keepclassmembers class **.R$* {
    *;
}
-keepclassmembers class * {

    void *(**On*Event);

}

#Tencent map sdk
#腾讯地图 2D sdk
#-libraryjars libs/TencentMapSDK_*_vx.x.x.x.jar
-keep class com.tencent.mapsdk.**{*;}
-keep class com.tencent.tencentmap.**{*;}
-keep class com.tencent.**{*;}

#腾讯地图检索sdk
#-libraryjars libs/TencentSearch_vx.x.x.x.jar
-keep class com.tencent.lbssearch.**{*;}
-keep class com.tencent.**{*;}

#腾讯地图街景sdk
#如果街景混淆报出类似"java.io.IOException: Can't read [*\TencentPanoramaSDKv1.1.0_15232.jar"
#请参考http://bbs.map.qq.com/forum.php?mod=viewthread&tid=21098&extra=page=1&filter=typeid&typeid=95&typeid=95
#-libraryjars libs/TencentStreetSDK_v.1.2.0_16324.jar
-keep class com.tencent.tencentmap.streetviewsdk.**{*;}
-keep class com.tencent.tencentmap.streetviewsdk.x{*;}
-keep class com.tencent.tencentmap.streetviewsdk.x
-keep class com.tencent.**{*;}
-dontwarn com.tencent.**
-dontwarn com.tencent.tencentmap.streetviewsdk.**
-dontwarn com.tencent.tencentmap.streetviewsdk.x



# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }


-dontwarn  android.location.Location
-dontwarn  org.eclipse.jdt.annotation.**
-dontnote ct.**
-keep  class ct.**{
        *;
}
-dontwarn ct.**
-dontwarn com.tencent.**


#忽略log
-dontskipnonpubliclibraryclasses
-dontusemixedcaseclassnames
-dontpreverify
-optimizationpasses 7
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes InnerClasses,EnclosingMethod

-keep class * extends android.app.Activity



#指定代码混淆的mapping.txt的输出目录
-dump class_files.txt
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
-printmapping build/outputs/mapping/release/mapping.txt



#-keep class com.nineoldandroids.**{*;}
#-dontwarn com.nineoldandroids.**


#报错:Warning: Exception while processing task java.io.IOException: Can't read [/Users/apple/code/ProMulti/app/E] (No such file or directory)
#-libraryjars E:\software\jdk\jre\lib\rt.jar
-keepattributes Annotation
#第三方和自己的so文件不用混淆
#这样再次崩溃的时候就有源文件和行号的信息了
-keepattributes SourceFile,LineNumberTable

#百度sdk
-keep class com.baidu.**{*;}
-dontwarn com.baidu.**
-keep class vi.com.gdi.bgl.android.**{*;}
#屏蔽所有警告，否则不能编译通过
-ignorewarnings


