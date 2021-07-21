# Consumer proguard rules for libandroid-navigation

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-dontwarn com.mapbox.android.core.location.**
-keep class com.mapbox.android.core.location.** { *; }

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# --- Java ---
-dontwarn java.awt.Color

# --- com.mapbox.api.directions.v5.MapboxDirections ---
-dontwarn com.sun.xml.internal.ws.spi.db.BindingContextFactory

# --- AutoValue ---
# AutoValue annotations are retained but dependency is compileOnly.
-dontwarn com.google.auto.value.**
