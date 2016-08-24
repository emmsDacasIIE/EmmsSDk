# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\adt-bundle-windows-x86_64-20140624\sdk/tools/proguard/proguard-android.txt
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
-libraryjars 'D:\Java\jdk1.8.0_91\jre\lib\rt.jar'

-libraryjars 'D:\Android\adt-bundle-windows-x86_64-20140624\adt-bundle-windows-x86_64-20140624\sdk\platforms\android-19\android.jar'

-optimizationpasses 5
-dontpreverify
-dontwarn
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings

# -keep public class * extends android.app.Activity

-keep class cn.emms.**{

public <fields>;

public <methods>;

}
-keep class cn.mcm.**{

public <fields>;

public <methods>;

}

-keep class com.baidu.location.** { *; }
-keep public class org.** {*;}