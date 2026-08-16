import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.hilt")
    id("river.android.test")
}

android {
    setNamespace("core.datastore")

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.datastore)

    testImplementation(libs.robolectric)
    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlin.test)
}
