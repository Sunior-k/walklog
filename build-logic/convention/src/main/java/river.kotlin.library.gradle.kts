import com.river.walklog.configureKotlin
import org.gradle.api.JavaVersion

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

configureKotlin()