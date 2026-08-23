package com.github.martyanovav.otuskotlin.fitbridge.training.common

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.permissions.FtcPrincipalRelation
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSession
import kotlin.time.Instant

interface IFBContext {
    var command: FBCommand
    var state: State
    val errors: List<FBError>
    var workMode: WorkMode
    var stubCase: Stubs
    var requestId: RequestId
    var timeStart: Instant
    var principal: AuthPrincipal
    var principalRelation: FtcPrincipalRelation
    var wsSession: IFBWsSession

    fun addError(error: FBError)
}
