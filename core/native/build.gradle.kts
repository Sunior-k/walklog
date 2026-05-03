import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
    id("river.android.hilt")
}

android {
    setNamespace("core.engine")

    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17", "-O2")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    api(projects.core.common)
    implementation(libs.litert)
    implementation(libs.litert.support)
}
