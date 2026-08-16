import com.river.walklog.setNamespace

plugins {
    id("river.android.feature")
    id("river.android.test")
}

android {
    setNamespace("feature.settings")

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.auth)
    implementation(projects.core.domain)
    implementation(projects.core.analytics)
    implementation(projects.core.ui)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.oss.licenses)
    testImplementation(projects.core.testing)
}
