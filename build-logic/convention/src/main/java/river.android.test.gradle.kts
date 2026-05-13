import com.river.walklog.configureAndroidJacocoReport
import com.river.walklog.configureAndroidTestOptions
import com.river.walklog.findLibrary

configureAndroidTestOptions()
configureAndroidJacocoReport()

dependencies {
    "androidTestImplementation"(findLibrary("androidx-runner").get())
}
