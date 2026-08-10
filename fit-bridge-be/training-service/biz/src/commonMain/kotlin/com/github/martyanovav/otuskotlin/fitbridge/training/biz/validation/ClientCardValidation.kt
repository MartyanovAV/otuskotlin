package com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

private const val DISPLAY_NAME_MAX_LENGTH = 120
private const val NOTE_MAX_LENGTH = 1000
private const val SEARCH_STRING_MAX_LENGTH = 120
private const val PAGE_SIZE_MIN = 1
private const val PAGE_SIZE_MAX = 100
private val ALLOWED_STATUSES = setOf("", "ACTIVE", "ARCHIVED")

private val IFBContext.clientCardContext: ClientCardContext
    get() = this as ClientCardContext

fun ICorChainDsl<IFBContext>.prepareClientCardValidation(
    title: String,
    resetIdentity: Boolean = false,
) = worker {
    this.title = title
    on { this is ClientCardContext }
    handle {
        val ctx = clientCardContext
        ctx.clientCardValidating = ctx.clientCardRequest.deepCopy().apply {
            id = ClientCardId(id.asString().trim())
            trainerId = trainerId.trim()
            displayName = displayName.trim()
            note = note.trim()
            lock = lock.trim()
            if (resetIdentity) {
                id = ClientCardId.NONE
                trainerId = ""
                isArchived = false
                lock = ""
            }
        }
    }
}

fun ICorChainDsl<IFBContext>.prepareClientCardFilterValidation(title: String) = worker {
    this.title = title
    on { this is ClientCardContext }
    handle {
        val ctx = clientCardContext
        ctx.clientCardFilterValidating = ctx.clientCardFilter.deepCopy().apply {
            status = status.trim().uppercase()
            searchString = searchString.trim()
        }
    }
}

fun ICorChainDsl<IFBContext>.validateClientCardIdNotEmpty(title: String) = validationWorker(
    title = title,
    field = "id",
    violationCode = "empty",
    description = "field must not be empty",
) { clientCardContext.clientCardValidating.id == ClientCardId.NONE }

fun ICorChainDsl<IFBContext>.validateClientCardIdFormat(title: String) = validationWorker(
    title = title,
    field = "id",
    violationCode = "badFormat",
    description = "field must contain only letters, numbers and ID separators",
) {
    val id = clientCardContext.clientCardValidating.id
    id != ClientCardId.NONE && !id.asString().matches(ID_PATTERN)
}

fun ICorChainDsl<IFBContext>.validateClientCardDisplayNameNotEmpty(title: String) = validationWorker(
    title = title,
    field = "displayName",
    violationCode = "empty",
    description = "field must not be empty",
) { clientCardContext.clientCardValidating.displayName.isEmpty() }

fun ICorChainDsl<IFBContext>.validateClientCardDisplayNameMaxLength(title: String) = validationWorker(
    title = title,
    field = "displayName",
    violationCode = "tooLong",
    description = "field must contain no more than $DISPLAY_NAME_MAX_LENGTH characters",
) { clientCardContext.clientCardValidating.displayName.length > DISPLAY_NAME_MAX_LENGTH }

fun ICorChainDsl<IFBContext>.validateClientCardDisplayNameHasContent(title: String) = validationWorker(
    title = title,
    field = "displayName",
    violationCode = "noContent",
    description = "field must contain letters",
) {
    val displayName = clientCardContext.clientCardValidating.displayName
    displayName.isNotEmpty() && !displayName.contains(HAS_LETTER)
}

fun ICorChainDsl<IFBContext>.validateClientCardNoteMaxLength(title: String) = validationWorker(
    title = title,
    field = "note",
    violationCode = "tooLong",
    description = "field must contain no more than $NOTE_MAX_LENGTH characters",
) { clientCardContext.clientCardValidating.note.length > NOTE_MAX_LENGTH }

fun ICorChainDsl<IFBContext>.validateClientCardLockNotEmpty(title: String) = validationWorker(
    title = title,
    field = "lock",
    violationCode = "empty",
    description = "field must not be empty",
) { clientCardContext.clientCardValidating.lock.isEmpty() }

fun ICorChainDsl<IFBContext>.validateClientCardLockFormat(title: String) = validationWorker(
    title = title,
    field = "lock",
    violationCode = "badFormat",
    description = "field must contain only letters, numbers and ID separators",
) {
    val lock = clientCardContext.clientCardValidating.lock
    lock.isNotEmpty() && !lock.matches(ID_PATTERN)
}

fun ICorChainDsl<IFBContext>.validateClientCardSearchStringLength(title: String) = validationWorker(
    title = title,
    field = "searchString",
    violationCode = "tooLong",
    description = "field must contain no more than $SEARCH_STRING_MAX_LENGTH characters",
) { clientCardContext.clientCardFilterValidating.searchString.length > SEARCH_STRING_MAX_LENGTH }

fun ICorChainDsl<IFBContext>.validateClientCardFilterStatus(title: String) = validationWorker(
    title = title,
    field = "status",
    violationCode = "unsupported",
    description = "field must be ACTIVE or ARCHIVED",
) { clientCardContext.clientCardFilterValidating.status !in ALLOWED_STATUSES }

fun ICorChainDsl<IFBContext>.validateClientCardPageNumber(title: String) = validationWorker(
    title = title,
    field = "pageNumber",
    violationCode = "outOfRange",
    description = "field must be greater than or equal to 1",
) { clientCardContext.clientCardFilterValidating.pageNumber < 1 }

fun ICorChainDsl<IFBContext>.validateClientCardPageSize(title: String) = validationWorker(
    title = title,
    field = "pageSize",
    violationCode = "outOfRange",
    description = "field must be in range $PAGE_SIZE_MIN..$PAGE_SIZE_MAX",
) { clientCardContext.clientCardFilterValidating.pageSize !in PAGE_SIZE_MIN..PAGE_SIZE_MAX }

fun ICorChainDsl<IFBContext>.finishClientCardValidation(title: String) = worker {
    this.title = title
    on { state == State.RUNNING && this is ClientCardContext }
    handle {
        val ctx = clientCardContext
        ctx.clientCardValidated = ctx.clientCardValidating
    }
}

fun ICorChainDsl<IFBContext>.finishClientCardFilterValidation(title: String) = worker {
    this.title = title
    on { state == State.RUNNING && this is ClientCardContext }
    handle {
        val ctx = clientCardContext
        ctx.clientCardFilterValidated = ctx.clientCardFilterValidating
    }
}
