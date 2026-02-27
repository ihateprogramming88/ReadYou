# MuPDF JNI classes must not be obfuscated
-keep class com.artifex.mupdf.fitz.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Domain models
-keep class org.openpdf.core.model.** { *; }

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
