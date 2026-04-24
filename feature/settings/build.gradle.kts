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
    implementation(projects.core.analytics)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
}
