import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.callstack.react.brownfield")
    `maven-publish`
    id("com.facebook.react")
}

react {
    autolinkLibrariesWithApp()
}

repositories {
    mavenCentral()
}

// jscFlavor는 android/app/build.gradle과 동일한 값을 사용 (Hermes 비활성 시 폴백)
val jscFlavor = "io.github.react-native-community:jsc-android:2026004.+"

android {
    namespace = "com.river.walklog.rewardstore.reactnativeapp"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        buildConfigField("boolean", "IS_EDGE_TO_EDGE_ENABLED", properties["edgeToEdgeEnabled"].toString())
        buildConfigField("boolean", "IS_NEW_ARCHITECTURE_ENABLED", properties["newArchEnabled"].toString())
        buildConfigField("boolean", "IS_HERMES_ENABLED", properties["hermesEnabled"].toString())
    }

    publishing {
        multipleVariants {
            allVariants()
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenAar") {
            groupId = "com.river.walklog"
            artifactId = "reactnativeapp"
            version = "0.0.1-local"
            afterEvaluate {
                from(components.getByName("default"))
            }

            pom {
                withXml {
                    // react-native 서드파티 의존성은 AAR에 이미 임베드돼 있으므로 pom.xml에서 제외.
                    val dependenciesNode = (asNode().get("dependencies") as groovy.util.NodeList).first() as groovy.util.Node
                    dependenciesNode.children()
                        .filterIsInstance<groovy.util.Node>()
                        .filter { (it.get("groupId") as groovy.util.NodeList).text() == rootProject.name }
                        .forEach { dependenciesNode.remove(it) }
                }
            }
        }
    }

    repositories {
        mavenLocal()
    }
}

dependencies {
    // 버전을 직접 지정하지 않고 android/app/build.gradle과 동일한 방식(React Native Gradle
    // Plugin의 자동 버전 치환)에 맡긴다 — RN 버전별 hermes-android 매칭 버전을 수동으로 잘못
    // 지정해 런타임 크래시가 나는 것을 피하기 위함.
    api("com.facebook.react:react-android")

    if (project.properties["hermesEnabled"].toString().toBoolean()) {
        api("com.facebook.react:hermes-android")
    } else {
        api(jscFlavor)
    }
}

val moduleBuildDir: Directory = layout.buildDirectory.get()

tasks.register("removeDependenciesFromModuleFile") {
    doLast {
        file("$moduleBuildDir/publications/mavenAar/module.json").run {
            val json = inputStream().use { JsonSlurper().parse(it) as Map<String, Any> }
            (json["variants"] as? List<MutableMap<String, Any>>)?.forEach { variant ->
                (variant["dependencies"] as? MutableList<Map<String, Any>>)?.removeAll { it["group"] == rootProject.name }
            }
            writer().use { it.write(JsonOutput.prettyPrint(JsonOutput.toJson(json))) }
        }
    }
}

tasks.named("generateMetadataFileForMavenAarPublication") {
    finalizedBy("removeDependenciesFromModuleFile")
}
