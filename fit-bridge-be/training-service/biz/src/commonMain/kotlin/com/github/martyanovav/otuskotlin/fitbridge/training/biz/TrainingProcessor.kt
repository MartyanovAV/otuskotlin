package com.github.martyanovav.otuskotlin.fitbridge.training.biz

import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.clientCardOperation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.initStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.trainingPlanOperation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubCannotArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubClientCardSuccess
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubNoCase
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubNotFound
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubTrainingPlanSuccess
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubValidationBadId
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubValidationBadPlanTitle
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.cor.rootChain
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker

class TrainingProcessor(
    @Suppress("unused") private val corSettings: CorSettings,
) {
    suspend fun exec(ctx: IFBContext) = businessChain.exec(ctx)

    private val businessChain = rootChain<IFBContext> {
        initStatus("Инициализация статуса")

        worker {
            this.title = "Проверка, что prod/test пока не реализованы"
            on { workMode != WorkMode.STUB }
            handle {
                state = State.FAILING
                addError(FBError(code = "not-implemented", group = "business", field = "", message = "Training business logic is not implemented yet"))
            }
        }

        // ClientCard Commands
        clientCardOperation("Создание карточки клиента", ClientCardCommand.CREATE) {
            stubs("Обработка стабов") {
                stubClientCardSuccess("Успешная обработка")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
        clientCardOperation("Чтение карточки клиента", ClientCardCommand.READ) {
            stubs("Обработка стабов") {
                stubClientCardSuccess("Успешная обработка")
                stubNotFound("Не найдено")
                stubValidationBadId("Неверный ID")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
        clientCardOperation("Обновление карточки клиента", ClientCardCommand.UPDATE) {
            stubs("Обработка стабов") {
                stubClientCardSuccess("Успешная обработка")
                stubNotFound("Не найдено")
                stubValidationBadId("Неверный ID")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
        clientCardOperation("Архивирование карточки клиента", ClientCardCommand.ARCHIVE) {
            stubs("Обработка стабов") {
                stubClientCardSuccess("Успешная обработка")
                stubNotFound("Не найдено")
                stubValidationBadId("Неверный ID")
                stubCannotArchive("Невозможно архивировать")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
        clientCardOperation("Поиск карточки клиента", ClientCardCommand.SEARCH) {
            stubs("Обработка стабов") {
                stubClientCardSuccess("Успешная обработка")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }

        // TrainingPlan Commands
        trainingPlanOperation("Создание тренировочного плана", TrainingPlanCommand.CREATE) {
            stubs("Обработка стабов") {
                stubTrainingPlanSuccess("Успешная обработка")
                stubValidationBadPlanTitle("Неверное название")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
        trainingPlanOperation("Чтение тренировочного плана", TrainingPlanCommand.READ) {
            stubs("Обработка стабов") {
                stubTrainingPlanSuccess("Успешная обработка")
                stubNotFound("Не найдено")
                stubValidationBadId("Неверный ID")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
        trainingPlanOperation("Обновление тренировочного плана", TrainingPlanCommand.UPDATE) {
            stubs("Обработка стабов") {
                stubTrainingPlanSuccess("Успешная обработка")
                stubNotFound("Не найдено")
                stubValidationBadId("Неверный ID")
                stubValidationBadPlanTitle("Неверное название")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
        trainingPlanOperation("Архивирование тренировочного плана", TrainingPlanCommand.ARCHIVE) {
            stubs("Обработка стабов") {
                stubTrainingPlanSuccess("Успешная обработка")
                stubNotFound("Не найдено")
                stubValidationBadId("Неверный ID")
                stubCannotArchive("Невозможно архивировать")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
        trainingPlanOperation("Поиск тренировочного плана", TrainingPlanCommand.SEARCH) {
            stubs("Обработка стабов") {
                stubTrainingPlanSuccess("Успешная обработка")
                stubNoCase("Ошибка: запрошенный стаб недопустим")
            }
        }
    }.build()
}
