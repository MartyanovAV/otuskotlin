package com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.CircuitItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.SupersetItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus

private const val TITLE_MIN_LENGTH = 3
private const val TITLE_MAX_LENGTH = 120
private const val ITEM_TITLE_MAX_LENGTH = 160
private const val ITEM_DESCRIPTION_MAX_LENGTH = 2000
private const val PLAN_ITEMS_MAX_COUNT = 200
private const val PLAN_ITEMS_MAX_DEPTH = 5
private const val SEARCH_STRING_MAX_LENGTH = 120
private const val PAGE_SIZE_MIN = 1
private const val PAGE_SIZE_MAX = 100
private val ALLOWED_STATUSES = setOf("", "ACTIVE", "ARCHIVED")
private val UUID_PATTERN = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
)

private val IFBContext.trainingPlanContext: TrainingPlanContext
    get() = this as TrainingPlanContext

private data class NormalizationFrame(
    val item: PlanItem,
    val childrenAreNormalized: Boolean = false,
)

private data class PlanItemAtDepth(
    val item: PlanItem,
    val depth: Int,
)

private fun List<PlanItem>.normalizedCopies(): MutableList<PlanItem> {
    val pending = ArrayDeque<NormalizationFrame>()
    asReversed().forEach { pending.addLast(NormalizationFrame(it)) }
    val normalized = mutableListOf<PlanItem>()

    while (pending.isNotEmpty()) {
        val frame = pending.removeLast()
        val item = frame.item
        when {
            item is ExerciseItem -> normalized += item.copy(
                id = item.id.trim(),
                title = item.title.trim(),
                description = item.description.trim(),
                exerciseId = item.exerciseId.trim(),
                sets = item.sets.map { set ->
                    set.copy(
                        reps = set.reps.trim(),
                        weight = set.weight.trim(),
                        weightUnit = set.weightUnit.trim(),
                    )
                }.toMutableList(),
            )

            !frame.childrenAreNormalized -> {
                pending.addLast(frame.copy(childrenAreNormalized = true))
                item.children().asReversed().forEach { pending.addLast(NormalizationFrame(it)) }
            }

            else -> {
                val childCount = item.children().size
                val childStart = normalized.size - childCount
                val children = normalized.subList(childStart, normalized.size).toMutableList()
                normalized.subList(childStart, normalized.size).clear()
                normalized += when (item) {
                    is CircuitItem -> item.copy(
                        id = item.id.trim(),
                        title = item.title.trim(),
                        description = item.description.trim(),
                        items = children,
                    )

                    is SupersetItem -> item.copy(
                        id = item.id.trim(),
                        title = item.title.trim(),
                        description = item.description.trim(),
                        items = children,
                    )

                    is ExerciseItem -> error("Exercise items are normalized before this branch")
                }
            }
        }
    }

    return normalized
}

private fun PlanItem.children(): List<PlanItem> = when (this) {
    is ExerciseItem -> emptyList()
    is CircuitItem -> items
    is SupersetItem -> items
}

private fun List<PlanItem>.flattened(): List<PlanItem> {
    val pending = ArrayDeque<PlanItem>()
    asReversed().forEach(pending::addLast)
    val flattened = mutableListOf<PlanItem>()

    while (pending.isNotEmpty()) {
        val item = pending.removeLast()
        flattened += item
        item.children().asReversed().forEach(pending::addLast)
    }

    return flattened
}

private fun List<PlanItem>.maxDepth(): Int {
    val pending = ArrayDeque<PlanItemAtDepth>()
    asReversed().forEach { pending.addLast(PlanItemAtDepth(it, 1)) }
    var maxDepth = 0

    while (pending.isNotEmpty()) {
        val current = pending.removeLast()
        maxDepth = maxOf(maxDepth, current.depth)
        current.item.children().asReversed().forEach {
            pending.addLast(PlanItemAtDepth(it, current.depth + 1))
        }
    }

    return maxDepth
}

fun ICorChainDsl<IFBContext>.prepareTrainingPlanValidation(
    title: String,
    resetIdentity: Boolean = false,
) = worker {
    this.title = title
    on { this is TrainingPlanContext }
    handle {
        val ctx = trainingPlanContext
        ctx.trainingPlanValidating = ctx.trainingPlanRequest.copy(
            planItems = ctx.trainingPlanRequest.planItems.normalizedCopies(),
        ).apply {
            id = TrainingPlanId(id.asString().trim())
            clientCardId = ClientCardId(clientCardId.asString().trim())
            trainerId = trainerId.trim()
            this.title = this.title.trim()
            lock = lock.trim()
            if (resetIdentity) {
                id = TrainingPlanId.NONE
                trainerId = ""
                status = TrainingPlanStatus.ACTIVE
                lock = ""
            }
        }
    }
}

fun ICorChainDsl<IFBContext>.prepareTrainingPlanFilterValidation(title: String) = worker {
    this.title = title
    on { this is TrainingPlanContext }
    handle {
        val ctx = trainingPlanContext
        ctx.trainingPlanFilterValidating = ctx.trainingPlanFilter.deepCopy().apply {
            clientCardId = ClientCardId(clientCardId.asString().trim())
            status = status.trim().uppercase()
            searchString = searchString.trim()
        }
    }
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanIdNotEmpty(title: String) = validationWorker(
    title = title,
    field = "id",
    violationCode = "empty",
    description = "field must not be empty",
) { trainingPlanContext.trainingPlanValidating.id == TrainingPlanId.NONE }

fun ICorChainDsl<IFBContext>.validateTrainingPlanIdFormat(title: String) = validationWorker(
    title = title,
    field = "id",
    violationCode = "badFormat",
    description = "field must contain only letters, numbers and ID separators",
) {
    val id = trainingPlanContext.trainingPlanValidating.id
    id != TrainingPlanId.NONE && !id.asString().matches(ID_PATTERN)
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanClientCardIdNotEmpty(title: String) = validationWorker(
    title = title,
    field = "clientCardId",
    violationCode = "empty",
    description = "field must not be empty",
) { trainingPlanContext.trainingPlanValidating.clientCardId == ClientCardId.NONE }

fun ICorChainDsl<IFBContext>.validateTrainingPlanClientCardIdFormat(title: String) = validationWorker(
    title = title,
    field = "clientCardId",
    violationCode = "badFormat",
    description = "field must contain only letters, numbers and ID separators",
) {
    val id = trainingPlanContext.trainingPlanValidating.clientCardId
    id != ClientCardId.NONE && !id.asString().matches(ID_PATTERN)
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanTitleNotEmpty(title: String) = validationWorker(
    title = title,
    field = "title",
    violationCode = "empty",
    description = "field must not be empty",
) { trainingPlanContext.trainingPlanValidating.title.isEmpty() }

fun ICorChainDsl<IFBContext>.validateTrainingPlanTitleMinLength(title: String) = validationWorker(
    title = title,
    field = "title",
    violationCode = "tooShort",
    description = "field must contain at least $TITLE_MIN_LENGTH characters",
) {
    val value = trainingPlanContext.trainingPlanValidating.title
    value.isNotEmpty() && value.length < TITLE_MIN_LENGTH
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanTitleMaxLength(title: String) = validationWorker(
    title = title,
    field = "title",
    violationCode = "tooLong",
    description = "field must contain no more than $TITLE_MAX_LENGTH characters",
) { trainingPlanContext.trainingPlanValidating.title.length > TITLE_MAX_LENGTH }

fun ICorChainDsl<IFBContext>.validateTrainingPlanTitleHasContent(title: String) = validationWorker(
    title = title,
    field = "title",
    violationCode = "noContent",
    description = "field must contain letters",
) {
    val value = trainingPlanContext.trainingPlanValidating.title
    value.isNotEmpty() && !value.contains(HAS_LETTER)
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemsNotEmpty(title: String) = validationWorker(
    title = title,
    field = "planItems",
    violationCode = "empty",
    description = "field must contain at least one plan item",
) { trainingPlanContext.trainingPlanValidating.planItems.isEmpty() }

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemCount(title: String) = validationWorker(
    title = title,
    field = "planItems",
    violationCode = "tooMany",
    description = "plan must contain no more than $PLAN_ITEMS_MAX_COUNT items including nested items",
) {
    trainingPlanContext.trainingPlanValidating.planItems.flattened().size > PLAN_ITEMS_MAX_COUNT
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemDepth(title: String) = validationWorker(
    title = title,
    field = "planItems",
    violationCode = "tooDeep",
    description = "plan item nesting depth must not exceed $PLAN_ITEMS_MAX_DEPTH",
) {
    trainingPlanContext.trainingPlanValidating.planItems.maxDepth() > PLAN_ITEMS_MAX_DEPTH
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemIds(title: String) = validationWorker(
    title = title,
    field = "planItems.id",
    violationCode = "badFormat",
    description = "every plan item ID must be a UUID",
) {
    trainingPlanContext.trainingPlanValidating.planItems
        .flattened()
        .any { !it.id.matches(UUID_PATTERN) }
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemIdsUnique(title: String) = validationWorker(
    title = title,
    field = "planItems.id",
    violationCode = "duplicate",
    description = "plan item IDs must be unique within the plan",
) {
    val ids = trainingPlanContext.trainingPlanValidating.planItems
        .flattened()
        .map { it.id }
        .filter { it.matches(UUID_PATTERN) }
    ids.size != ids.toSet().size
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemTitles(title: String) = validationWorker(
    title = title,
    field = "planItems.title",
    violationCode = "invalid",
    description = "every plan item title must contain letters and no more than $ITEM_TITLE_MAX_LENGTH characters",
) {
    trainingPlanContext.trainingPlanValidating.planItems
        .flattened()
        .any { it.title.isEmpty() || it.title.length > ITEM_TITLE_MAX_LENGTH || !it.title.contains(HAS_LETTER) }
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemDescriptions(title: String) = validationWorker(
    title = title,
    field = "planItems.description",
    violationCode = "tooLong",
    description = "every plan item description must contain no more than $ITEM_DESCRIPTION_MAX_LENGTH characters",
) {
    trainingPlanContext.trainingPlanValidating.planItems
        .flattened()
        .any { it.description.length > ITEM_DESCRIPTION_MAX_LENGTH }
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemCollections(title: String) = validationWorker(
    title = title,
    field = "planItems.items",
    violationCode = "invalidSize",
    description = "circuits must contain at least one item and supersets at least two items",
) {
    trainingPlanContext.trainingPlanValidating.planItems
        .flattened()
        .any {
            when (it) {
                is ExerciseItem -> false
                is CircuitItem -> it.items.isEmpty()
                is SupersetItem -> it.items.size < 2
            }
        }
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemRounds(title: String) = validationWorker(
    title = title,
    field = "planItems.rounds",
    violationCode = "outOfRange",
    description = "circuit rounds must be greater than or equal to 1",
) {
    trainingPlanContext.trainingPlanValidating.planItems
        .flattened()
        .filterIsInstance<CircuitItem>()
        .any { it.rounds < 1 }
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemDurations(title: String) = validationWorker(
    title = title,
    field = "planItems.sets.durationSeconds",
    violationCode = "outOfRange",
    description = "exercise set duration must be greater than or equal to 0",
) {
    trainingPlanContext.trainingPlanValidating.planItems
        .flattened()
        .filterIsInstance<ExerciseItem>()
        .any { item -> item.sets.any { it.durationSeconds < 0 } }
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanItemRestSeconds(title: String) = validationWorker(
    title = title,
    field = "planItems.restSeconds",
    violationCode = "outOfRange",
    description = "rest duration must be greater than or equal to 0",
) {
    trainingPlanContext.trainingPlanValidating.planItems
        .flattened()
        .any {
            when (it) {
                is ExerciseItem -> it.restBetweenSetsSeconds < 0
                is CircuitItem -> it.restBetweenRoundsSeconds < 0
                is SupersetItem -> it.restBetweenSetsSeconds < 0
            }
        }
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanLockNotEmpty(title: String) = validationWorker(
    title = title,
    field = "lock",
    violationCode = "empty",
    description = "field must not be empty",
) { trainingPlanContext.trainingPlanValidating.lock.isEmpty() }

fun ICorChainDsl<IFBContext>.validateTrainingPlanLockFormat(title: String) = validationWorker(
    title = title,
    field = "lock",
    violationCode = "badFormat",
    description = "field must contain only letters, numbers and ID separators",
) {
    val lock = trainingPlanContext.trainingPlanValidating.lock
    lock.isNotEmpty() && !lock.matches(ID_PATTERN)
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanFilterClientCardIdFormat(title: String) = validationWorker(
    title = title,
    field = "clientCardId",
    violationCode = "badFormat",
    description = "field must contain only letters, numbers and ID separators",
) {
    val id = trainingPlanContext.trainingPlanFilterValidating.clientCardId
    id != ClientCardId.NONE && !id.asString().matches(ID_PATTERN)
}

fun ICorChainDsl<IFBContext>.validateTrainingPlanSearchStringLength(title: String) = validationWorker(
    title = title,
    field = "searchString",
    violationCode = "tooLong",
    description = "field must contain no more than $SEARCH_STRING_MAX_LENGTH characters",
) { trainingPlanContext.trainingPlanFilterValidating.searchString.length > SEARCH_STRING_MAX_LENGTH }

fun ICorChainDsl<IFBContext>.validateTrainingPlanFilterStatus(title: String) = validationWorker(
    title = title,
    field = "status",
    violationCode = "unsupported",
    description = "field must be ACTIVE or ARCHIVED",
) { trainingPlanContext.trainingPlanFilterValidating.status !in ALLOWED_STATUSES }

fun ICorChainDsl<IFBContext>.validateTrainingPlanPageNumber(title: String) = validationWorker(
    title = title,
    field = "pageNumber",
    violationCode = "outOfRange",
    description = "field must be greater than or equal to 1",
) { trainingPlanContext.trainingPlanFilterValidating.pageNumber < 1 }

fun ICorChainDsl<IFBContext>.validateTrainingPlanPageSize(title: String) = validationWorker(
    title = title,
    field = "pageSize",
    violationCode = "outOfRange",
    description = "field must be in range $PAGE_SIZE_MIN..$PAGE_SIZE_MAX",
) { trainingPlanContext.trainingPlanFilterValidating.pageSize !in PAGE_SIZE_MIN..PAGE_SIZE_MAX }

fun ICorChainDsl<IFBContext>.finishTrainingPlanValidation(title: String) = worker {
    this.title = title
    on { state == State.RUNNING && this is TrainingPlanContext }
    handle {
        val ctx = trainingPlanContext
        ctx.trainingPlanValidated = ctx.trainingPlanValidating
    }
}

fun ICorChainDsl<IFBContext>.finishTrainingPlanFilterValidation(title: String) = worker {
    this.title = title
    on { state == State.RUNNING && this is TrainingPlanContext }
    handle {
        val ctx = trainingPlanContext
        ctx.trainingPlanFilterValidated = ctx.trainingPlanFilterValidating
    }
}
