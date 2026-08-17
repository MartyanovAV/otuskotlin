package com.github.martyanovav.otuskotlin.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

@Suppress("unused")
class BuildPluginKtlint : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")

        extensions.configure<KtlintExtension> {
            verbose.set(true)

            reporters {
                reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
                reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
            }
        }
    }
}
