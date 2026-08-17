package com.github.martyanovav.otuskotlin.fitbridge.training.stubs

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId

object ClientCardStub {
    private val card =
        ClientCard(
            id = ClientCardId("00000000-0000-0000-0000-000000000101"),
            ownerId = "00000000-0000-0000-0000-000000000001",
            displayName = "Анна Смирнова",
            note = "Учебная карточка клиента",
            lock = "stub-lock-client-card",
        )

    fun get(): ClientCard = card.copy()

    fun getList(): List<ClientCard> = listOf(get())
}

object TrainingPlanStub {
    private val plan =
        TrainingPlan(
            id = TrainingPlanId("00000000-0000-0000-0000-000000000201"),
            clientCardId = ClientCardId("00000000-0000-0000-0000-000000000101"),
            ownerId = "00000000-0000-0000-0000-000000000001",
            title = "Базовая тренировка",
            lock = "stub-lock-training-plan",
            planItems =
                mutableListOf(
                    ExerciseItem(
                        id = "exercise-item-1",
                        title = "Приседания",
                        description = "Учебное упражнение",
                        exerciseId = "squat",
                        sets = mutableListOf(ExerciseSet(reps = "10", weight = "20")),
                        restBetweenSetsSeconds = 60,
                    ),
                ),
        )

    fun get(): TrainingPlan =
        plan.copy(
            planItems = plan.planItems.toMutableList(),
        )

    fun getList(): List<TrainingPlan> = listOf(get())
}
