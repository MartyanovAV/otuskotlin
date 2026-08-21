package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface PlanItem {
    val id: String
    val title: String
    val description: String
}

@Serializable
data class ExerciseSet(
    val reps: String = "",
    val weight: String = "",
    val weightUnit: String = "KG",
    val durationSeconds: Int = 0,
)

@Serializable
@SerialName("exercise")
data class ExerciseItem(
    override val id: String = "",
    override val title: String = "",
    override val description: String = "",
    val exerciseId: String = "",
    val sets: List<ExerciseSet> = emptyList(),
    val restBetweenSetsSeconds: Int = 0,
) : PlanItem

@Serializable
@SerialName("circuit")
data class CircuitItem(
    override val id: String = "",
    override val title: String = "",
    override val description: String = "",
    val rounds: Int = 1,
    val items: List<PlanItem> = emptyList(),
    val restBetweenRoundsSeconds: Int = 0,
) : PlanItem

@Serializable
@SerialName("superset")
data class SupersetItem(
    override val id: String = "",
    override val title: String = "",
    override val description: String = "",
    val items: List<PlanItem> = emptyList(),
    val restBetweenSetsSeconds: Int = 0,
) : PlanItem

fun PlanItem.deepCopy(): PlanItem =
    when (this) {
        is ExerciseItem -> copy(sets = sets.map { it.copy() })
        is CircuitItem -> copy(items = items.map { it.deepCopy() })
        is SupersetItem -> copy(items = items.map { it.deepCopy() })
    }
