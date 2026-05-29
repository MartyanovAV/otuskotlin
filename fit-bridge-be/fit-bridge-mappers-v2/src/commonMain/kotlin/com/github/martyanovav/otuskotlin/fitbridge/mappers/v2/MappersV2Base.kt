package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardListRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.DashboardGetTrainerSummaryRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Error
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PublicPlanMarkCompletionRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PublicPlanOpenByTokenRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileCreateOrUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileReadOwnRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanClosePublicLinkRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanGeneratePublicLinkRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadCompletionStatusRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.common.DashboardContext
import com.github.martyanovav.otuskotlin.fitbridge.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.common.PublicPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainerProfileContext
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.DashboardCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.PublicPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfileCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
import kotlin.time.Instant

// ─── ID conversion helpers ───────────────────────────────────────────────────

internal fun String?.toTrainerId() = this?.let { TrainerId(it) } ?: TrainerId.NONE

internal fun String?.toClientCardId() = this?.let { ClientCardId(it) } ?: ClientCardId.NONE

internal fun String?.toTrainingPlanId() = this?.let { TrainingPlanId(it) } ?: TrainingPlanId.NONE

internal fun String?.toRequestId() = this?.let { RequestId(it) } ?: RequestId.NONE

internal fun String?.toInstant() = this?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST

// ─── Debug helpers ───────────────────────────────────────────────────────────

internal fun Debug?.transportToWorkMode(): WorkMode = when (this?.mode) {
    RequestDebugMode.PROD -> WorkMode.PROD
    RequestDebugMode.TEST -> WorkMode.TEST
    RequestDebugMode.STUB -> WorkMode.STUB
    null -> WorkMode.PROD
}

internal fun Debug?.transportToStubCase(): Stubs = when (this?.stub) {
    RequestDebugStubs.SUCCESS -> Stubs.SUCCESS
    RequestDebugStubs.NOT_FOUND -> Stubs.NOT_FOUND
    RequestDebugStubs.BAD_ID -> Stubs.BAD_ID
    RequestDebugStubs.BAD_PUBLIC_NAME -> Stubs.BAD_PUBLIC_NAME
    RequestDebugStubs.BAD_PLAN_TITLE -> Stubs.BAD_PLAN_TITLE
    RequestDebugStubs.CANNOT_ARCHIVE -> Stubs.CANNOT_ARCHIVE
    null -> Stubs.NONE
    else -> Stubs.NONE
}

// ─── Base context setup ──────────────────────────────────────────────────────

internal fun IFBContext.fromTransportBase(reqId: String?, debug: Debug?) {
    requestId = reqId.toRequestId()
    workMode = debug.transportToWorkMode()
    stubCase = debug.transportToStubCase()
}

// ─── Error conversion ────────────────────────────────────────────────────────

internal fun List<FBError>.toTransportErrors(): List<Error>? = this
    .map { it.toTransport() }
    .takeIf { it.isNotEmpty() }

internal fun FBError.toTransport() = Error(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() }
)

// ─── Dispatch: IRequest.fromTransport() ──────────────────────────────────────

fun IRequest.fromTransport(): IFBContext = when (this) {
    is TrainerProfileReadOwnRequest -> TrainerProfileContext().apply { fromTransport(this@fromTransport) }
    is TrainerProfileCreateOrUpdateRequest -> TrainerProfileContext().apply { fromTransport(this@fromTransport) }
    is ClientCardCreateRequest -> ClientCardContext().apply { fromTransport(this@fromTransport) }
    is ClientCardReadRequest -> ClientCardContext().apply { fromTransport(this@fromTransport) }
    is ClientCardUpdateRequest -> ClientCardContext().apply { fromTransport(this@fromTransport) }
    is ClientCardArchiveRequest -> ClientCardContext().apply { fromTransport(this@fromTransport) }
    is ClientCardListRequest -> ClientCardContext().apply { fromTransport(this@fromTransport) }
    is TrainingPlanCreateRequest -> TrainingPlanContext().apply { fromTransport(this@fromTransport) }
    is TrainingPlanReadRequest -> TrainingPlanContext().apply { fromTransport(this@fromTransport) }
    is TrainingPlanUpdateRequest -> TrainingPlanContext().apply { fromTransport(this@fromTransport) }
    is TrainingPlanArchiveRequest -> TrainingPlanContext().apply { fromTransport(this@fromTransport) }
    is TrainingPlanGeneratePublicLinkRequest -> TrainingPlanContext().apply { fromTransport(this@fromTransport) }
    is TrainingPlanClosePublicLinkRequest -> TrainingPlanContext().apply { fromTransport(this@fromTransport) }
    is TrainingPlanReadCompletionStatusRequest -> TrainingPlanContext().apply { fromTransport(this@fromTransport) }
    is DashboardGetTrainerSummaryRequest -> DashboardContext().apply { fromTransport(this@fromTransport) }
    is PublicPlanOpenByTokenRequest -> PublicPlanContext().apply { fromTransport(this@fromTransport) }
    is PublicPlanMarkCompletionRequest -> PublicPlanContext().apply { fromTransport(this@fromTransport) }
}

// ─── Dispatch: IFBContext.toTransport() ──────────────────────────────────────

fun IFBContext.toTransport(): IResponse = when (val cmd = command) {
    TrainerProfileCommand.READ_OWN -> (this as TrainerProfileContext).toTransportTrainerProfileReadOwn()
    TrainerProfileCommand.CREATE_OR_UPDATE -> (this as TrainerProfileContext).toTransportTrainerProfileCreateOrUpdate()
    ClientCardCommand.CREATE -> (this as ClientCardContext).toTransportClientCardCreate()
    ClientCardCommand.READ -> (this as ClientCardContext).toTransportClientCardRead()
    ClientCardCommand.UPDATE -> (this as ClientCardContext).toTransportClientCardUpdate()
    ClientCardCommand.ARCHIVE -> (this as ClientCardContext).toTransportClientCardArchive()
    ClientCardCommand.LIST -> (this as ClientCardContext).toTransportClientCardList()
    TrainingPlanCommand.CREATE -> (this as TrainingPlanContext).toTransportTrainingPlanCreate()
    TrainingPlanCommand.READ -> (this as TrainingPlanContext).toTransportTrainingPlanRead()
    TrainingPlanCommand.UPDATE -> (this as TrainingPlanContext).toTransportTrainingPlanUpdate()
    TrainingPlanCommand.ARCHIVE -> (this as TrainingPlanContext).toTransportTrainingPlanArchive()
    TrainingPlanCommand.GENERATE_PUBLIC_LINK -> (this as TrainingPlanContext).toTransportTrainingPlanGeneratePublicLink()
    TrainingPlanCommand.CLOSE_PUBLIC_LINK -> (this as TrainingPlanContext).toTransportTrainingPlanClosePublicLink()
    TrainingPlanCommand.READ_COMPLETION_STATUS -> (this as TrainingPlanContext).toTransportTrainingPlanReadCompletionStatus()
    DashboardCommand.GET_TRAINER_SUMMARY -> (this as DashboardContext).toTransportDashboardGetTrainerSummary()
    PublicPlanCommand.OPEN_BY_TOKEN -> (this as PublicPlanContext).toTransportPublicPlanOpenByToken()
    PublicPlanCommand.MARK_COMPLETION -> (this as PublicPlanContext).toTransportPublicPlanMarkCompletion()
    else -> throw IllegalArgumentException("Unsupported command $cmd")
}
