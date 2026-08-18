package com.github.martyanovav.otuskotlin.fitbridge.training.biz.exceptions

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode

class FbDbNotConfiguredException(val workMode: WorkMode) : Exception(
    "Database is not configured properly for workmode $workMode",
)
