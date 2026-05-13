import com.river.walklog.setNamespace

plugins {
    id("river.android.feature")
    id("river.android.test")
}

android {
    setNamespace("feature.history")

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.analytics)
    implementation(projects.core.ui)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(projects.core.testing)
}
