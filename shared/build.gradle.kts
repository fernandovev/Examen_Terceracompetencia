import dev.detekt.gradle.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
}

detekt {
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin"
    )
    config.setFrom(files("${rootDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = true
}

tasks.withType<Detekt>().configureEach {
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**")
}

tasks.register<Detekt>("detektAll") {
    description = "Analiza todo el código fuente KMP"
    group = "verification"

    setSource(files("src"))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**")

    reports {
        html.required.set(true)
        sarif.required.set(true)
    }
}

kotlin {

    android {
        namespace = "org.ucb.appp1.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.work.runtime.ktx)

            implementation(libs.androidx.room3.sqlite.wrapper)
            implementation(libs.androidx.sqlite.android)
            implementation(libs.firebase.messaging)
            implementation(libs.firebase.messaging)
            implementation("androidx.core:core-ktx:1.12.0")
            // Firebase oficial para Android.
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:35.0.2"))
            implementation("com.google.firebase:firebase-config")
            implementation("com.google.firebase:firebase-firestore")

// Permite esperar las operaciones de Firebase desde corrutinas.
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation("io.arrow-kt:arrow-core:1.2.1")
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.firebase.database)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation(libs.sentry.kotlin.multiplatform)
            implementation("edu.ucb.ds:shared:1.0.0-SNAPSHOT") {
                isChanging = true
            }

            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.firebase.config)
            implementation(libs.firebase.firestore)

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }


    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

configurations.configureEach {
    if (name.contains("Ios") || name.contains("ios") || name.contains("Jvm") || name.contains("jvm")) {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
        exclude(group = "androidx.work", module = "work-runtime-ktx")
        exclude(group = "androidx.work", module = "work-runtime")
        exclude(group = "edu.ucb.ds", module = "shared")
    }
}

val locoApiKey = providers.gradleProperty("LOCO_API_KEY").orElse("TU_API_KEY_AQUI")
tasks.register("downloadLocaliseTranslations") {
    group = "localization"
    description = "Descarga las traducciones desde Loco y las ubica en composeResources"

    notCompatibleWithConfigurationCache("Descarga externa desde la API de Loco")

    doLast {
        val apiKey = locoApiKey.get()

        if (apiKey == "TU_API_TOKEN_AQUI") {
            throw GradleException("Por favor, configura LOCO_API_KEY en tu archivo gradle.properties de la raíz.")
        }

        println("Iniciando descarga de traducciones desde Loco (localise.biz)...")

        val urlString = "https://localise.biz/api/export/archive/xml.zip?key=$apiKey"
        val url = URL(urlString)

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
        }

        val responseCode = connection.responseCode
        if (responseCode == 200) {
            val outputZip = layout.buildDirectory.file("translations.zip").get().asFile
            val tempExtractDir = layout.buildDirectory.dir("temp_translations").get().asFile
            val targetDir = projectDir.resolve("src/commonMain/composeResources")

            connection.inputStream.use { input ->
                outputZip.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (tempExtractDir.exists()) tempExtractDir.deleteRecursively()
            tempExtractDir.mkdirs()

            ZipInputStream(outputZip.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val newFile = File(tempExtractDir, entry.name)
                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        newFile.outputStream().use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            val foundValuesDir = tempExtractDir.walkTopDown().find { it.isDirectory && it.name.startsWith("values") }?.parentFile
                ?: tempExtractDir

            foundValuesDir.listFiles()?.forEach { file ->
                if (file.isDirectory && file.name.startsWith("values")) {
                    val destination = File(targetDir, file.name)
                    destination.deleteRecursively()
                    file.copyRecursively(destination, overwrite = true)
                    println("Copiado idioma: ${file.name} -> ${destination.absolutePath}")
                }
            }

            tempExtractDir.deleteRecursively()
            println("Traducciones organizadas con exito en ${targetDir.absolutePath}")
        } else {
            val errorResponse = connection.errorStream?.bufferedReader()?.readText() ?: "Error desconocido"
            throw GradleException("Error al conectar con Loco ($responseCode): $errorResponse")
        }
    }
}