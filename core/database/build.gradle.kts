import com.river.walklog.setNamespace
plugins {
    id("river.android.library")
    id("river.android.hilt")
    alias(libs.plugins.room)
}

android {
    setNamespace("core.database")

    room {
        schemaDirectory("$projectDir/schemas")
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    api(projects.core.model)

    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.robolectric)
    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(projects.core.testing)
}
