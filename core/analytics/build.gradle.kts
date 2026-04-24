import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
}

android {
    setNamespace("core.analytics")
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
}
