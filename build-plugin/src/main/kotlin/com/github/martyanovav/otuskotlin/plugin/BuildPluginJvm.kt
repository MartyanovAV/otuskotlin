package com.github.martyanovav.otuskotlin.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

@Suppress("unused")
internal class BuildPluginJvm : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        val libs = the<LibrariesForLibs>()
        pluginManager.apply("org.jetbrains.kotlin.jvm")

//        pluginManager.apply(KotlinPlatformJvmPlugin::class.java)
        group = rootProject.group
        version = rootProject.version

        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(libs.versions.jvm.language.get().toInt())
        }

        tasks.withType(JavaCompile::class.java).configureEach {
            sourceCompatibility = libs.versions.jvm.language.get()
            targetCompatibility = libs.versions.jvm.compiler.get()
        }

        repositories {
            mavenCentral()
        }
    }
}
