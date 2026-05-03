import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.hilt")
}

android {
    setNamespace("core.analytics")
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
}
