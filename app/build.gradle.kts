import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    id("river.android.application")
    id("river.android.compose")
    id("river.android.test")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.baselineprofile)
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "com.river.walklog"

    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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

// reward-store-rn(별도 Gradle 루트)에서 발행한 브라운필드 AAR은 com.facebook.react:hermes-android를
// 버전 없이 요구한다. 이 module은 React Native Gradle Plugin이 아니라서 자동 버전 치환이 없으므로,
// RN 0.86.2가 실제로 사용하는 새 Hermes 아티팩트(com.facebook.hermes:hermes-android)로 직접 치환한다.
// (버전은 reward-store-rn 빌드 시 Gradle이 실제로 내려받은 버전을 확인해 고정한 값)
configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.facebook.react:hermes-android"))
            .using(module("com.facebook.hermes:hermes-android:250829098.0.16"))
    }
}

dependencies {
    // React Native 브라운필드 — 리워드 스토어 (reward-store-rn/android/reactnativeapp에서 패키징)
    implementation("com.river.walklog:reactnativeapp:0.0.1-local")
    implementation("com.facebook.react:react-android:0.86.2")

    // Sync modules
    implementation(projects.sync.work)

    // Feature modules
    implementation(projects.feature.recap)
    implementation(projects.feature.home)
    implementation(projects.feature.mission)
    implementation(projects.feature.report)
    implementation(projects.feature.widget)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.login)
    implementation(projects.feature.settings)
    implementation(projects.feature.history)
    implementation(projects.feature.reward)

    // Hilt + WorkManager (WalkLogApplication Configuration.Provider)
    implementation(libs.androidx.work.runtime)
    implementation(libs.hilt.work)

    // Firebase (Crashlytics, Analytics — BOM은 app에서 선언)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Core modules (for DI wiring at app level)
    implementation(projects.core.analytics)
    implementation(projects.core.auth)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain) // RewardBridgeModule에서 UseCase 직접 사용
    implementation(projects.core.ui) // ThemeViewModel (테마 팩 전체 적용)

    // Material (BottomNavigationView)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Navigation — Fragment (XML base)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Splash Screen API (core-splashscreen 1.0.1)
    implementation(libs.androidx.core.splashscreen)

    // Activity / Fragment / AppCompat
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    // ThemeViewModel(core:ui)을 각 Fragment에서 hiltViewModel()로 직접 사용하기 위함
    implementation(libs.hilt.navigation.compose)

    // Baseline Profile
    implementation(libs.androidx.profileinstaller)

    baselineProfile(projects.benchmark)

    androidTestImplementation(libs.androidx.runner)
    testImplementation(projects.core.testing)
}
