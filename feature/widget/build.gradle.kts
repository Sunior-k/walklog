import com.river.walklog.setNamespace

plugins {
    id("river.android.feature")
}

android {
    setNamespace("feature.widget")
}

dependencies {
    implementation(projects.core.domain)

    // Glance AppWidget
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // WorkManager + Hilt Worker
    implementation(libs.androidx.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // CrashReporter 인터페이스 — Firebase 직접 의존 없이 사용
    implementation(projects.core.analytics)
}
