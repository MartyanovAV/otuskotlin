import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("build-kmp")
    alias(libs.plugins.crowdproj.generator)
    alias(libs.plugins.kotlinx.serialization)
}

// 1. Настраиваем конфигурацию для получения файла из другого проекта
val specsFromLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    specsFromLib(project(path = ":training-specs", configuration = "specsConfiguration"))
}

val specDir = layout.buildDirectory.dir("specs")

tasks {
    val extractLibSpecs by registering(Copy::class) {
        dependsOn(specsFromLib)
        // Распаковываем ZIP-файл (он будет единственным в этой конфигурации)
        from(specsFromLib.elements.map { it.map { file -> zipTree(file) } })
        into(specDir)
    }

// 3. Привязываем генерацию к распаковке
    named("openApiGenerate") {
        dependsOn(extractLibSpecs)
    }

    val openApiGenerateTask: GenerateTask = getByName("openApiGenerate", GenerateTask::class) {
        outputDir.set(layout.buildDirectory.file("generate-resources").get().toString())
        configOptions.set(
            mapOf(
                "dateLibrary" to "string",
                "enumPropertyNaming" to "UPPERCASE",
                "serializationLibrary" to "kotlinx-serialization",
                "collectionType" to "list"
            )
        )
    }

    filter { it.name.startsWith("compile") }.forEach {
        it.dependsOn(openApiGenerateTask)
    }

    withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask> {
        dependsOn(openApiGenerateTask)
    }
}

crowdprojGenerate {
    packageName.set("${project.group}.api.v2")
    inputSpec.set(specDir.map { it.file("specs-training-v2.yaml").asFile.absolutePath })
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDirs(layout.buildDirectory.dir("generate-resources/src/commonMain/kotlin"))
            dependencies {
                implementation(kotlin("stdlib-common"))

                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}
