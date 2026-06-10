package com.github.martyanovav.otuskotlin.fitbridge.profile.common

import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.stubs.Stubs
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
