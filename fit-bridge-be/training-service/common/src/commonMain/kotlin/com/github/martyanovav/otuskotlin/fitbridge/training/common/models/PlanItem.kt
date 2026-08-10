package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

sealed interface PlanItem {
    val id: String
    val title: String
    val description: String
}

data class ExerciseSet(
    var reps: String = "",
    var weight: String = "",
    var weightUnit: String = "KG",
    var durationSeconds: Int = 0
)

data class ExerciseItem(
    override val id: String = "",
    override val title: String = "",
    override val description: String = "",
    var exerciseId: String = "",
    var sets: MutableList<ExerciseSet> = mutableListOf(),
    var restBetweenSetsSeconds: Int = 0
) : PlanItem

data class CircuitItem(
    override val id: String = "",
    override val title: String = "",
    override val description: String = "",
    var rounds: Int = 1,
    var items: MutableList<PlanItem> = mutableListOf(),
    var restBetweenRoundsSeconds: Int = 0
) : PlanItem

data class SupersetItem(
    override val id: String = "",
    override val title: String = "",
    override val description: String = "",
    var items: MutableList<PlanItem> = mutableListOf(),
    var restBetweenSetsSeconds: Int = 0
) : PlanItem

fun PlanItem.deepCopy(): PlanItem = when (this) {
    is ExerciseItem -> copy(sets = sets.map { it.copy() }.toMutableList())
    is CircuitItem -> copy(items = items.map { it.deepCopy() }.toMutableList())
    is SupersetItem -> copy(items = items.map { it.deepCopy() }.toMutableList())
}
