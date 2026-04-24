import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.compose")
}

android {
    setNamespace("core.designsystem")
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    implementation(libs.coil.svg)
}
