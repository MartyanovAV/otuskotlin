package com.github.martyanovav.otuskotlin.fitbridge.training.biz

import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.cor.rootChain
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.access.accessValidationTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.access.checkClientCardOwner
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.access.initTrainingPlanUserIds
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.access.resolveTrainingPlanRelation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.initStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.operation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.initTrainingPlanRepo
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.prepareRepoResult
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoComplete
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoCreate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoPrepareActivate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoPrepareArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoPrepareComplete
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoPrepareCreate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoPrepareUpdate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoRead
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoSearch
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoUpdate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubCannotArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubNoCase
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubNotFound
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubTrainingPlanSuccess
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubValidationBadId
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubValidationBadPlanTitle
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.finishTrainingPlanFilterValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.finishTrainingPlanValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.prepareTrainingPlanFilterValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.prepareTrainingPlanValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanClientCardIdFormat
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanClientCardIdNotEmpty
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanFilterClientCardIdFormat
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanFilterStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanIdFormat
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanIdNotEmpty
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemCollections
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemCount
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemDepth
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemDescriptions
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemDurations
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemIds
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemIdsUnique
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemRestSeconds
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemRounds
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemTitles
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanItemsNotEmpty
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanLockFormat
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanLockNotEmpty
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanPageNumber
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanPageSize
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanSearchStringLength
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanTitleHasContent
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanTitleMaxLength
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanTitleMinLength
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateTrainingPlanTitleNotEmpty
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validation
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanCorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand

class TrainingPlanProcessor(
    private val settings: TrainingPlanCorSettings
) {
    suspend fun exec(ctx: TrainingPlanContext) {
        ctx.corSettings = settings
        businessChain.exec(ctx)
    }

    private val businessChain =
        rootChain<TrainingPlanContext> {
            initStatus("Инициализация статуса")

            // TrainingPlan Commands
            operation("Создание тренировочного плана", TrainingPlanCommand.CREATE) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubValidationBadPlanTitle("Неверное название")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация создания тренировочного плана") {
                    prepareTrainingPlanValidation("Подготовка тренировочного плана", resetIdentity = true)
                    validateTrainingPlanClientCardIdNotEmpty("Проверка непустого ID карточки клиента")
                    validateTrainingPlanClientCardIdFormat("Проверка формата ID карточки клиента")
                    validateTrainingPlanTitleNotEmpty("Проверка непустого названия")
                    validateTrainingPlanTitleMinLength("Проверка минимальной длины названия")
                    validateTrainingPlanTitleMaxLength("Проверка максимальной длины названия")
                    validateTrainingPlanTitleHasContent("Проверка содержимого названия")
                    validateTrainingPlanItemsNotEmpty("Проверка наличия элементов плана")
                    validateTrainingPlanItemCount("Проверка количества элементов плана")
                    validateTrainingPlanItemDepth("Проверка глубины вложенности элементов плана")
                    validateTrainingPlanItemIds("Проверка идентификаторов элементов плана")
                    validateTrainingPlanItemIdsUnique("Проверка уникальности идентификаторов элементов плана")
                    validateTrainingPlanItemTitles("Проверка названий элементов плана")
                    validateTrainingPlanItemDescriptions("Проверка описаний элементов плана")
                    validateTrainingPlanItemCollections("Проверка состава групп элементов плана")
                    validateTrainingPlanItemRounds("Проверка количества кругов")
                    validateTrainingPlanItemDurations("Проверка длительности подходов")
                    validateTrainingPlanItemRestSeconds("Проверка длительности отдыха")
                    finishTrainingPlanValidation("Завершение валидации тренировочного плана")
                }
                chain {
                    title = "Логика сохранения"
                    resolveTrainingPlanRelation("Вычисление прав доступа")
                    accessValidationTrainingPlan("Проверка прав доступа")
                    checkClientCardOwner("Проверка владельца ClientCard")
                    initTrainingPlanUserIds("Установка владельца и автора")
                    trainingPlanRepoPrepareCreate("Подготовка объекта для сохранения")
                    trainingPlanRepoCreate("Создание тренировочного плана в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Чтение тренировочного плана", TrainingPlanCommand.READ) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация чтения тренировочного плана") {
                    prepareTrainingPlanValidation("Подготовка тренировочного плана")
                    validateTrainingPlanIdNotEmpty("Проверка непустого ID")
                    validateTrainingPlanIdFormat("Проверка формата ID")
                    finishTrainingPlanValidation("Завершение валидации тренировочного плана")
                }
                chain {
                    title = "Логика чтения"
                    trainingPlanRepoRead("Чтение тренировочного плана из БД")
                    resolveTrainingPlanRelation("Вычисление прав доступа")
                    accessValidationTrainingPlan("Проверка прав доступа")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Обновление тренировочного плана", TrainingPlanCommand.UPDATE) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubValidationBadPlanTitle("Неверное название")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация обновления тренировочного плана") {
                    prepareTrainingPlanValidation("Подготовка тренировочного плана")
                    validateTrainingPlanIdNotEmpty("Проверка непустого ID")
                    validateTrainingPlanIdFormat("Проверка формата ID")
                    validateTrainingPlanTitleNotEmpty("Проверка непустого названия")
                    validateTrainingPlanTitleMinLength("Проверка минимальной длины названия")
                    validateTrainingPlanTitleMaxLength("Проверка максимальной длины названия")
                    validateTrainingPlanTitleHasContent("Проверка содержимого названия")
                    validateTrainingPlanItemsNotEmpty("Проверка наличия элементов плана")
                    validateTrainingPlanItemCount("Проверка количества элементов плана")
                    validateTrainingPlanItemDepth("Проверка глубины вложенности элементов плана")
                    validateTrainingPlanItemIds("Проверка идентификаторов элементов плана")
                    validateTrainingPlanItemIdsUnique("Проверка уникальности идентификаторов элементов плана")
                    validateTrainingPlanItemTitles("Проверка названий элементов плана")
                    validateTrainingPlanItemDescriptions("Проверка описаний элементов плана")
                    validateTrainingPlanItemCollections("Проверка состава групп элементов плана")
                    validateTrainingPlanItemRounds("Проверка количества кругов")
                    validateTrainingPlanItemDurations("Проверка длительности подходов")
                    validateTrainingPlanItemRestSeconds("Проверка длительности отдыха")
                    validateTrainingPlanLockNotEmpty("Проверка непустого lock")
                    validateTrainingPlanLockFormat("Проверка формата lock")
                    finishTrainingPlanValidation("Завершение валидации тренировочного плана")
                }
                chain {
                    title = "Логика сохранения"
                    trainingPlanRepoRead("Чтение тренировочного плана из БД")
                    resolveTrainingPlanRelation("Вычисление прав доступа")
                    accessValidationTrainingPlan("Проверка прав доступа")
                    trainingPlanRepoPrepareUpdate("Подготовка объекта для обновления")
                    trainingPlanRepoUpdate("Обновление тренировочного плана в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Архивирование тренировочного плана", TrainingPlanCommand.ARCHIVE) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubCannotArchive("Невозможно архивировать")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация архивирования тренировочного плана") {
                    prepareTrainingPlanValidation("Подготовка тренировочного плана")
                    validateTrainingPlanIdNotEmpty("Проверка непустого ID")
                    validateTrainingPlanIdFormat("Проверка формата ID")
                    validateTrainingPlanLockNotEmpty("Проверка непустого lock")
                    validateTrainingPlanLockFormat("Проверка формата lock")
                    finishTrainingPlanValidation("Завершение валидации тренировочного плана")
                }
                chain {
                    title = "Логика архивирования"
                    trainingPlanRepoRead("Чтение тренировочного плана из БД")
                    resolveTrainingPlanRelation("Вычисление прав доступа")
                    accessValidationTrainingPlan("Проверка прав доступа")
                    trainingPlanRepoPrepareArchive("Подготовка объекта для архивирования")
                    trainingPlanRepoArchive("Архивирование тренировочного плана в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Завершение тренировочного плана", TrainingPlanCommand.COMPLETE) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация завершения тренировочного плана") {
                    prepareTrainingPlanValidation("Подготовка тренировочного плана")
                    validateTrainingPlanIdNotEmpty("Проверка непустого ID")
                    validateTrainingPlanIdFormat("Проверка формата ID")
                    validateTrainingPlanLockNotEmpty("Проверка непустого lock")
                    validateTrainingPlanLockFormat("Проверка формата lock")
                    finishTrainingPlanValidation("Завершение валидации тренировочного плана")
                }
                chain {
                    title = "Логика завершения"
                    trainingPlanRepoRead("Чтение тренировочного плана из БД")
                    resolveTrainingPlanRelation("Вычисление прав доступа")
                    accessValidationTrainingPlan("Проверка прав доступа")
                    trainingPlanRepoPrepareComplete("Подготовка объекта для завершения")
                    trainingPlanRepoComplete("Завершение тренировочного плана в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Активация тренировочного плана", TrainingPlanCommand.ACTIVATE) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация активации тренировочного плана") {
                    prepareTrainingPlanValidation("Подготовка тренировочного плана")
                    validateTrainingPlanIdNotEmpty("Проверка непустого ID")
                    validateTrainingPlanIdFormat("Проверка формата ID")
                    validateTrainingPlanLockNotEmpty("Проверка непустого lock")
                    validateTrainingPlanLockFormat("Проверка формата lock")
                    finishTrainingPlanValidation("Завершение валидации тренировочного плана")
                }
                chain {
                    title = "Логика активации"
                    trainingPlanRepoRead("Чтение тренировочного плана из БД")
                    resolveTrainingPlanRelation("Вычисление прав доступа")
                    accessValidationTrainingPlan("Проверка прав доступа")
                    trainingPlanRepoPrepareActivate("Подготовка объекта для активации")
                    trainingPlanRepoUpdate("Активация тренировочного плана в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Поиск тренировочного плана", TrainingPlanCommand.SEARCH) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация поиска тренировочных планов") {
                    prepareTrainingPlanFilterValidation("Подготовка фильтра тренировочных планов")
                    validateTrainingPlanFilterClientCardIdFormat("Проверка ID карточки клиента")
                    validateTrainingPlanSearchStringLength("Проверка длины строки поиска")
                    validateTrainingPlanFilterStatus("Проверка статуса плана")
                    validateTrainingPlanPageNumber("Проверка номера страницы")
                    validateTrainingPlanPageSize("Проверка размера страницы")
                    finishTrainingPlanFilterValidation("Завершение валидации фильтра планов")
                }
                resolveTrainingPlanRelation("Вычисление прав доступа")
                accessValidationTrainingPlan("Проверка прав доступа")
                trainingPlanRepoSearch("Поиск тренировочных планов в БД по фильтру")
                prepareRepoResult("Подготовка ответа")
            }
        }.build()
}
