# Preserve Application class and other important classes
-keep class com.raafat.revise.MainApplication { *; }

# Keep all activities and view models
-keep class com.raafat.revise.main.** { *; }
-keep class com.raafat.revise.di.** { *; }
-keep class com.raafat.revise.model.** { *; }
-keep class com.raafat.revise.persistence.** { *; }

# Keep all public methods and fields in view models
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <methods>;
    public <fields>;
}

# Keep all annotations
-keepattributes *Annotation*

# Keep Gson, Jackson, and similar libraries from obfuscation (if you use them)
-keep class com.google.gson.** { *; }

# Keep Room Database related classes (if using Room)
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.db.** { *; }
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.Dao { *; }

# Keep data binding classes (if using data binding)
-keep class androidx.databinding.** { *; }
-keep class **.databinding.** { *; }

# Keep any classes with specific names or annotations that are used by reflection
-keep class * {
    @com.google.gson.annotations.SerializedName *;
}

# Shrink and optimize
-dontwarn **.R$*
-dontwarn **.BuildConfig
-dontwarn androidx.**

# Ignore specific warnings (example)
-dontwarn javax.**

# Keep class names for debugging
-keepattributes SourceFile, LineNumberTable
