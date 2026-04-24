# =============================================================================
# WalkLog ProGuard / R8 Rules
# 참고: https://developer.android.com/build/shrink-code
#       https://github.com/android/nowinandroid
# =============================================================================

# ── Crashlytics: 스택 트레이스 가독성 유지 ────────────────────────────────────
# 소스 파일명·라인 번호를 DEX에 유지 → 대시보드에서 실제 위치 확인 가능
-keepattributes SourceFile,LineNumberTable
# 난독화된 클래스명을 "SourceFile"로 통일 → 소스 파일명 노출 방지
-renamesourcefileattribute SourceFile
# 커스텀 예외 클래스 유지 (Crashlytics Issues 탭 분류에 필요)
-keep public class * extends java.lang.Exception

# ── Annotation 유지 (Hilt·Room·Kotlin 등 런타임 어노테이션 의존) ──────────────
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
# AtomicFU volatile 필드 — R8가 제거하면 coroutine 상태기계가 깨짐
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Room ──────────────────────────────────────────────────────────────────────
# Entity 필드는 리플렉션으로 읽히므로 이름 변경 금지
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
# Hilt 생성 클래스 (_HiltModules, _ComponentTreeDeps 등) 유지
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-dontwarn dagger.**
-dontwarn javax.inject.**

# ── WorkManager ───────────────────────────────────────────────────────────────
# Worker 서브클래스는 WorkManager가 클래스명으로 인스턴스화함
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── Glance (App Widget) ───────────────────────────────────────────────────────
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-dontwarn androidx.glance.**

# ── Jetpack Navigation (Fragment XML) ────────────────────────────────────────
-keepnames class androidx.navigation.fragment.NavHostFragment
-dontwarn androidx.navigation.**

# ── Firebase / Crashlytics ────────────────────────────────────────────────────
# Firebase SDK consumer rules로 대부분 처리됨; 추가 안전망
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── Kotlinx Serialization (추후 API 통신 도입 시 활성화) ──────────────────────
# -dontnote kotlinx.serialization.AnnotationsKt
# -keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
# -keepclasseswithmembers class kotlinx.serialization.** {
#     kotlinx.serialization.KSerializer serializer(...);
# }
# -keep,includedescriptorclasses class com.river.walklog.**$$serializer { *; }

# ── Retrofit / OkHttp (추후 네트워크 레이어 도입 시 활성화) ──────────────────
# -dontwarn okhttp3.**
# -dontwarn retrofit2.**
# -keep class retrofit2.** { *; }
# -keepclassmembernames,allowobfuscation interface * {
#     @retrofit2.http.* <methods>;
# }
