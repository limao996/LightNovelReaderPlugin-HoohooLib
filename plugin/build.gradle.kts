import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.limao996.hoohoolib"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.limao996.hoohoolib"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach {
            val outputImpl = it as com.android.build.api.variant.impl.VariantOutputImpl
            val originalFileName = outputImpl.outputFileName.get()
            val newFileName = originalFileName.replace(".apk", ".apk.lnrp")
            outputImpl.outputFileName = newFileName
        }
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // 宿主内置依赖
    compileOnly(libs.kotlinx.coroutines.core)
    compileOnly(libs.androidx.runtime)
    compileOnly(libs.androidx.navigation.runtime.ktx)
    compileOnly(libs.androidx.foundation.layout)
    compileOnly(platform(libs.compose.bom))
    compileOnly(libs.compose.material3)
    implementation(libs.lightnovelreader.api)

    // 插件依赖
    implementation(libs.jsoup)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
}

val debugHostPkg = "indi.dmzz_yyhyy.lightnovelreader.debug"
val releaseHostPkg = "indi.dmzz_yyhyy.lightnovelreader"


fun pluginApk(): File =
    File(layout.buildDirectory.asFile.get(), "outputs/apk/debug").walkTopDown().first {
        it.isFile && it.name.endsWith(".apk") || it.name.endsWith(".lnrp")
    }

fun installPluginTask(name: String, hostPkg: String) {
    tasks.register(name) {
        group = "plugin"
        dependsOn("assembleDebug")

        doLast {
            val adb =
                listOf(androidComponents.sdkComponents.adb.get().asFile.absolutePath) + (System.getenv(
                    "ANDROID_SERIAL"
                )?.let { listOf("-s", it) } ?: emptyList())
            val src = pluginApk()
            val file = if (src.name.endsWith(".apk")) src
            else File(src.parent, src.name.removeSuffix(".lnrp")).also { src.renameTo(it) }

            try {
                providers.exec {
                    commandLine(adb + listOf("install", "-r", "-t", file))
                }.result.get()
            } finally {
                if (file != src) file.renameTo(src)
            }

            providers.exec {
                commandLine(adb + listOf("shell", "am", "force-stop", hostPkg))
            }.result.get()

            providers.exec {
                commandLine(
                    adb + listOf(
                        "shell",
                        "monkey",
                        "-p",
                        hostPkg,
                        "-c",
                        "android.intent.category.LAUNCHER",
                        "1"
                    )
                )
            }.result.get()
        }
    }
}

installPluginTask("runDebugHost", debugHostPkg)
installPluginTask("runReleaseHost", releaseHostPkg)
