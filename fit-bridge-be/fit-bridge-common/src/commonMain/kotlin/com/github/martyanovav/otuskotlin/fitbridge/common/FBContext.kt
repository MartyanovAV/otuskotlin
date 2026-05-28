package com.github.martyanovav.otuskotlin.fitbridge.common

import kotlinx.datetime.Instant
import com.github.martyanovav.otuskotlin.fitbridge.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs

data class FBContext(
    var command: FBCommand = FBCommand.NONE,
    var state: State = State.NONE,
    val errors: MutableList<FBError> = mutableListOf(),

    var workMode: WorkMode = WorkMode.PROD,
    var stubCase: Stubs = Stubs.NONE,

    var requestId: RequestId = RequestId.NONE,
    var timeStart: Instant = Instant.DISTANT_PAST,

    var trainerProfileRequest: TrainerProfile = TrainerProfile(),
    var clientCardRequest: ClientCard = ClientCard(),
    var trainingPlanRequest: TrainingPlan = TrainingPlan(),

    var trainerProfileResponse: TrainerProfile = TrainerProfile(),
    var clientCardResponse: ClientCard = ClientCard(),
    var clientCardsResponse: MutableList<ClientCard> = mutableListOf(),
    var trainingPlanResponse: TrainingPlan = TrainingPlan(),
)
