package com.github.martyanovav.otuskotlin.fitbridge.training.common

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.MpLoggerProvider
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSessionRepo

data class CorSettings(
    val loggerProvider: MpLoggerProvider = MpLoggerProvider(),
    val wsSessionsV1: IFBWsSessionRepo = IFBWsSessionRepo.NONE,
    val wsSessionsV2: IFBWsSessionRepo = IFBWsSessionRepo.NONE,
)
