import com.river.walklog.configureKotlinAndroid
import com.river.walklog.findLibrary

plugins {
    id("com.android.library")
}

configureKotlinAndroid()

dependencies {
    "androidTestImplementation"(findLibrary("androidx-runner").get())
}
