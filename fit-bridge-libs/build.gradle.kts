plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "com.github.martyanovav.otuskotlin.fitbridge.libs"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

ext {
    val specDir = layout.projectDirectory.dir("../specs")
    set("spec-v1", specDir.file("specs-ad-v1.yaml").toString())
    set("spec-v2", specDir.file("specs-ad-v2.yaml").toString())
}

tasks {
    arrayOf("build", "clean", "check").forEach {tsk ->
        register(tsk ) {
            group = "build"
            dependsOn(subprojects.map {  it.getTasksByName(tsk,false)})
        }
    }

    register("ktlintFormat") {
        description = "Форматирование кода ktlint для всех подпроектов"
        group = "formatting"
        dependsOn(subprojects.map { it.getTasksByName("ktlintFormat", false) })
    }

    register("ktlintCheck") {
        description = "Проверка кода ktlint для всех подпроектов"
        group = "verification"
        dependsOn(subprojects.map { it.getTasksByName("ktlintCheck", false) })
    }
}
