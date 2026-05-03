import com.river.walklog.setNamespace
import java.util.Properties

plugins {
    id("river.android.library")
    id("river.android.hilt")
    id("river.android.test")
}

android {
    setNamespace("core.network")

    buildFeatures {
        buildConfig = true
    }

    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use(::load)
        }
    }
    val kmaServiceKey = localProperties.getProperty("KMA_SERVICE_KEY")
        ?: providers.environmentVariable("KMA_SERVICE_KEY").orNull
        ?: ""

    defaultConfig {
        buildConfigField("String", "KMA_SERVICE_KEY", "\"$kmaServiceKey\"")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.coroutines.android)
    implementation(libs.okhttp.logging)
}
