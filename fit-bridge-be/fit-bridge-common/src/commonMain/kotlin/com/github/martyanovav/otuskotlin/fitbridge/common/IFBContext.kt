package com.github.martyanovav.otuskotlin.fitbridge.common

import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
import kotlin.time.Instant

interface IFBContext {
    var command: FBCommand
    var state: State
    val errors: List<FBError>
    var workMode: WorkMode
    var stubCase: Stubs
    var requestId: RequestId
    var timeStart: Instant

    fun addError(error: FBError)
}
