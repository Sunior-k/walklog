plugins {
    id("river.kotlin.library")
    id("river.kotlin.hilt")
    id("river.kotlin.test")
}

dependencies {
    implementation(libs.coroutines.core)
}
