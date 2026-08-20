package com.github.martyanovav.otuskotlin.fitbridge.training.common

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.permissions.FtcPrincipalRelation
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSession
import kotlin.time.Instant

data class TrainingPlanContext(
    override var command: FBCommand = TrainingPlanCommand.NONE,
    override var state: State = State.NONE,
    override val errors: MutableList<FBError> = mutableListOf(),
    override var workMode: WorkMode = WorkMode.PROD,
    override var stubCase: Stubs = Stubs.NONE,
    override var requestId: RequestId = RequestId.NONE,
    override var timeStart: Instant = Instant.DISTANT_PAST,
    override var principal: AuthPrincipal = AuthPrincipal.NONE,
    override var principalRelation: FtcPrincipalRelation = FtcPrincipalRelation.NONE,
    override var wsSession: IFBWsSession = IFBWsSession.NONE,
    override var corSettings: CorSettings = CorSettings(),
    var trainingPlanRequest: TrainingPlan = TrainingPlan(),
    var trainingPlanValidating: TrainingPlan = TrainingPlan(),
    var trainingPlanValidated: TrainingPlan = TrainingPlan(),
    var trainingPlanFilter: TrainingPlanFilter = TrainingPlanFilter(),
    var trainingPlanFilterValidating: TrainingPlanFilter = TrainingPlanFilter(),
    var trainingPlanFilterValidated: TrainingPlanFilter = TrainingPlanFilter(),
    var trainingPlanRepo: IRepoTrainingPlan = IRepoTrainingPlan.NONE,
    var clientCardRepo: IRepoClientCard = IRepoClientCard.NONE,
    var trainingPlanRepoRead: TrainingPlan = TrainingPlan(),
    var trainingPlanRepoPrepare: TrainingPlan = TrainingPlan(),
    var trainingPlanRepoDone: TrainingPlan = TrainingPlan(),
    var trainingPlansRepoDone: MutableList<TrainingPlan> = mutableListOf(),
    var trainingPlanResponse: TrainingPlan = TrainingPlan(),
    var trainingPlansResponse: Page<TrainingPlan> = Page(),
) : IFBContext {
    override fun addError(error: FBError) {
        errors.add(error)
    }
}
