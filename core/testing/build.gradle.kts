import com.river.walklog.setNamespace

plugins {
    id("river.android.library")
}

android {
    setNamespace("core.testing")
}

dependencies {
    api(projects.core.auth)
    api(projects.core.data)
    api(projects.core.model)
    api(libs.junit4)
    api(libs.coroutines.test)
    api(libs.mockk)
    api(libs.turbine)
    api(libs.kotlin.test)
}
