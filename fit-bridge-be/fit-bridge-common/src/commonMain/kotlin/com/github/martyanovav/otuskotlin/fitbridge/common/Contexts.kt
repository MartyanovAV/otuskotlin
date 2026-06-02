package com.github.martyanovav.otuskotlin.fitbridge.common

import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardFilter
import com.github.martyanovav.otuskotlin.fitbridge.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfileCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlanFilter
import com.github.martyanovav.otuskotlin.fitbridge.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
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

data class ClientCardContext(
    override var command: FBCommand = ClientCardCommand.NONE,
    override var state: State = State.NONE,
    override val errors: MutableList<FBError> = mutableListOf(),
    override var workMode: WorkMode = WorkMode.PROD,
    override var stubCase: Stubs = Stubs.NONE,
    override var requestId: RequestId = RequestId.NONE,
    override var timeStart: Instant = Instant.DISTANT_PAST,

    var clientCardRequest: ClientCard = ClientCard(),
    var clientCardResponse: ClientCard = ClientCard(),
    var clientCardsResponse: Page<ClientCard> = Page(),
    var clientCardFilter: ClientCardFilter = ClientCardFilter(),
) : IFBContext {
    override fun addError(error: FBError) {
        errors.add(error)
    }
}

data class TrainingPlanContext(
    override var command: FBCommand = TrainingPlanCommand.NONE,
    override var state: State = State.NONE,
    override val errors: MutableList<FBError> = mutableListOf(),
    override var workMode: WorkMode = WorkMode.PROD,
    override var stubCase: Stubs = Stubs.NONE,
    override var requestId: RequestId = RequestId.NONE,
    override var timeStart: Instant = Instant.DISTANT_PAST,

    var trainingPlanRequest: TrainingPlan = TrainingPlan(),
    var trainingPlanFilter: TrainingPlanFilter = TrainingPlanFilter(),

    var trainingPlanResponse: TrainingPlan = TrainingPlan(),
    var trainingPlansResponse: Page<TrainingPlan> = Page(),
) : IFBContext {
    override fun addError(error: FBError) {
        errors.add(error)
    }
}
