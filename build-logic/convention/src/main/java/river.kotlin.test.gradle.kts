import com.river.walklog.configureKotlinJacocoReport
import com.river.walklog.findLibrary

configureKotlinJacocoReport()

dependencies {
    "testImplementation"(findLibrary("junit4").get())
    "testImplementation"(findLibrary("mockk").get())
    "testImplementation"(findLibrary("turbine").get())
    "testImplementation"(findLibrary("coroutines-test").get())
    "testImplementation"(findLibrary("kotlin-test").get())
}
