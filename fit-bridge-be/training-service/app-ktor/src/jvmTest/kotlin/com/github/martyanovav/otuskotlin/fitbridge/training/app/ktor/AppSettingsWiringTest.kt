package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import kotlin.test.Test
import kotlin.test.assertSame

class AppSettingsWiringTest {
    @Test
    fun trainingPlanSettingsReuseClientCardRepositories() {
        val settings = AppSettings()

        assertSame(settings.ccCorSettings.repoClientCardTest, settings.tpCorSettings.repoClientCardTest)
        assertSame(settings.ccCorSettings.repoClientCardProd, settings.tpCorSettings.repoClientCardProd)
        assertSame(settings.ccCorSettings.repoClientCardStub, settings.tpCorSettings.repoClientCardStub)
    }
}
