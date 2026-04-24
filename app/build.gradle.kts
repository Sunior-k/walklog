import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    id("river.android.application")
    id("river.android.compose")
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.river.walklog"

    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        targetSdk = 35
    }

    buildTypes {
        getByName("debug") {
            // Debug 빌드: Crashlytics 매핑 파일 생성 비활성화
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            // Release 빌드: ProGuard 매핑 파일을 Crashlytics에 업로드
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = true
                nativeSymbolUploadEnabled = true
            }
        }
        // Baseline Profile 생성 전용 빌드타입
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Feature modules
    implementation(projects.feature.recap)
    implementation(projects.feature.home)
    implementation(projects.feature.mission)
    implementation(projects.feature.forecast)
    implementation(projects.feature.report)
    implementation(projects.feature.widget)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.settings)
    implementation(projects.feature.history)

    // Hilt + WorkManager (WalkLogApplication Configuration.Provider)
    implementation(libs.androidx.work.runtime)
    implementation(libs.hilt.work)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Core modules (for DI wiring at app level)
    implementation(projects.core.analytics)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.designsystem)

    // Material (BottomNavigationView)
    implementation(libs.material)

    // Navigation — Fragment (XML base)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Splash Screen API (core-splashscreen 1.0.1)
    implementation(libs.androidx.core.splashscreen)

    // Activity / Fragment / AppCompat
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    // Baseline Profile
    implementation(libs.androidx.profileinstaller)

    baselineProfile(projects.benchmark)
}
