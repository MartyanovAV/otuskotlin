package com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus

data class TrainingPlanEntity(
    val id: String? = null,
    val clientCardId: String? = null,
    val ownerId: String? = null,
    val title: String? = null,
    val status: String? = null,
    val lock: String? = null,
    val planItems: List<PlanItem> = emptyList(),
    val version: Int = 1,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    constructor(model: TrainingPlan) : this(
        id = model.id.asString().takeIf { it.isNotBlank() },
        clientCardId = model.clientCardId.asString().takeIf { it.isNotBlank() },
        ownerId = model.ownerId.takeIf { it.isNotBlank() },
        title = model.title.takeIf { it.isNotBlank() },
        status = model.status.takeIf { it != TrainingPlanStatus.NONE }?.name,
        lock = model.lock.takeIf { it.isNotBlank() },
        planItems = model.planItems,
        version = model.version,
        createdAt = model.createdAt.takeIf { it.isNotBlank() },
        updatedAt = model.updatedAt.takeIf { it.isNotBlank() },
    )

    fun toInternal() =
        TrainingPlan(
            id = id?.let { TrainingPlanId(it) } ?: TrainingPlanId.NONE,
            clientCardId = clientCardId?.let { ClientCardId(it) } ?: ClientCardId.NONE,
            ownerId = ownerId ?: "",
            title = title ?: "",
            status = status?.let { TrainingPlanStatus.valueOf(it) } ?: TrainingPlanStatus.NONE,
            lock = lock ?: "",
            planItems = planItems,
            version = version,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: "",
        )
}
