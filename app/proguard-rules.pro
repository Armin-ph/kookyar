# Kookyar R8 rules

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.kookyar.app.**$$serializer { *; }
-keepclassmembers class com.kookyar.app.** { *** Companion; }
-keepclasseswithmembers class com.kookyar.app.** { kotlinx.serialization.KSerializer serializer(...); }

# Coroutines debug metadata (avoids warnings)
-dontwarn kotlinx.coroutines.debug.*

# Keep AudioRecord/AudioTrack callbacks (reflection-free, but safe)
-keep class androidx.compose.runtime.** { *; }
