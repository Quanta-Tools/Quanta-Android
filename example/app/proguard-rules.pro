# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep the Quanta SDK classes
-keep class tools.quanta.sdk.** { *; }

# If your project uses Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keepclasseswithmembers class kotlinx.serialization.json.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$*Serializer { *; }

# Standard Android ProGuard rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
