package com.github.martyanovav.otuskotlin.fitbridge.profile.common

import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerProfileCommand
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.stubs.Stubs
import kotlin.time.Instant

data class TrainerProfileContext(
    override var command: FBCommand = TrainerProfileCommand.NONE,
    override var state: State = State.NONE,
    override val errors: MutableList<FBError> = mutableListOf(),
    override var workMode: WorkMode = WorkMode.PROD,
    override var stubCase: Stubs = Stubs.NONE,
    override var requestId: RequestId = RequestId.NONE,
    override var timeStart: Instant = Instant.DISTANT_PAST,

    var trainerProfileRequest: TrainerProfile = TrainerProfile(),
    var trainerProfileResponse: TrainerProfile = TrainerProfile(),
) : IFBContext {
    override fun addError(error: FBError) {
        errors.add(error)
    }
}
