package com.github.martyanovav.otuskotlin.fitbridge.training.biz

import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.cor.rootChain
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.clientCardOperation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.initStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.trainingPlanOperation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoCreate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoPrepareArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoPrepareCreate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoPrepareUpdate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoRead
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoSearch
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoUpdate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.initClientCardRepo
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.initTrainingPlanRepo
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.prepareRepoResult
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoCreate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoPrepareArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoPrepareCreate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoPrepareUpdate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoRead
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoSearch
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.trainingPlanRepoUpdate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubCannotArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubClientCardSuccess
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubNoCase
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubNotFound
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubTrainingPlanSuccess
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubValidationBadId
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubValidationBadPlanTitle
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.clientCardValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.finishClientCardFilterValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.finishClientCardValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.finishTrainingPlanFilterValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.finishTrainingPlanValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.prepareClientCardFilterValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.prepareClientCardValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.prepareTrainingPlanFilterValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.prepareTrainingPlanValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.trainingPlanValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardDisplayNameHasContent
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardDisplayNameMaxLength
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardDisplayNameNotEmpty
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardFilterStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardIdFormat
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardIdNotEmpty
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardLockFormat
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardLockNotEmpty
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardNoteMaxLength
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardPageNumber
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardPageSize
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validateClientCardSearchStringLength
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
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand

class TrainingProcessor(
    @Suppress("unused") private val corSettings: CorSettings,
) {
    suspend fun exec(ctx: IFBContext) {
        ctx.corSettings = corSettings
        businessChain.exec(ctx)
    }

    private val businessChain =
        rootChain<IFBContext> {
            initStatus("Инициализация статуса")

            // ClientCard Commands
            clientCardOperation("Создание карточки клиента", ClientCardCommand.CREATE) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                clientCardValidation("Валидация создания карточки клиента") {
                    prepareClientCardValidation("Подготовка карточки клиента", resetIdentity = true)
                    validateClientCardDisplayNameNotEmpty("Проверка непустого имени клиента")
                    validateClientCardDisplayNameMaxLength("Проверка длины имени клиента")
                    validateClientCardDisplayNameHasContent("Проверка содержимого имени клиента")
                    validateClientCardNoteMaxLength("Проверка длины заметки")
                    finishClientCardValidation("Завершение валидации карточки клиента")
                }
                chain {
                    title = "Логика сохранения"
                    clientCardRepoPrepareCreate("Подготовка объекта для сохранения")
                    clientCardRepoCreate("Создание карточки клиента в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            clientCardOperation("Чтение карточки клиента", ClientCardCommand.READ) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                clientCardValidation("Валидация чтения карточки клиента") {
                    prepareClientCardValidation("Подготовка карточки клиента")
                    validateClientCardIdNotEmpty("Проверка непустого ID")
                    validateClientCardIdFormat("Проверка формата ID")
                    finishClientCardValidation("Завершение валидации карточки клиента")
                }
                chain {
                    title = "Логика чтения"
                    clientCardRepoRead("Чтение карточки клиента из БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            clientCardOperation("Обновление карточки клиента", ClientCardCommand.UPDATE) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                clientCardValidation("Валидация обновления карточки клиента") {
                    prepareClientCardValidation("Подготовка карточки клиента")
                    validateClientCardIdNotEmpty("Проверка непустого ID")
                    validateClientCardIdFormat("Проверка формата ID")
                    validateClientCardDisplayNameNotEmpty("Проверка непустого имени клиента")
                    validateClientCardDisplayNameMaxLength("Проверка длины имени клиента")
                    validateClientCardDisplayNameHasContent("Проверка содержимого имени клиента")
                    validateClientCardNoteMaxLength("Проверка длины заметки")
                    validateClientCardLockNotEmpty("Проверка непустого lock")
                    validateClientCardLockFormat("Проверка формата lock")
                    finishClientCardValidation("Завершение валидации карточки клиента")
                }
                chain {
                    title = "Логика сохранения"
                    clientCardRepoRead("Чтение карточки клиента из БД")
                    clientCardRepoPrepareUpdate("Подготовка объекта для обновления")
                    clientCardRepoUpdate("Обновление карточки клиента в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            clientCardOperation("Архивирование карточки клиента", ClientCardCommand.ARCHIVE) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubCannotArchive("Невозможно архивировать")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                clientCardValidation("Валидация архивирования карточки клиента") {
                    prepareClientCardValidation("Подготовка карточки клиента")
                    validateClientCardIdNotEmpty("Проверка непустого ID")
                    validateClientCardIdFormat("Проверка формата ID")
                    validateClientCardLockNotEmpty("Проверка непустого lock")
                    validateClientCardLockFormat("Проверка формата lock")
                    finishClientCardValidation("Завершение валидации карточки клиента")
                }
                chain {
                    title = "Логика архивирования"
                    clientCardRepoRead("Чтение карточки клиента из БД")
                    clientCardRepoPrepareArchive("Подготовка объекта для архивирования")
                    clientCardRepoArchive("Архивирование карточки клиента в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            clientCardOperation("Поиск карточки клиента", ClientCardCommand.SEARCH) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                clientCardValidation("Валидация поиска карточек клиентов") {
                    prepareClientCardFilterValidation("Подготовка фильтра карточек клиентов")
                    validateClientCardSearchStringLength("Проверка длины строки поиска")
                    validateClientCardFilterStatus("Проверка статуса карточки")
                    validateClientCardPageNumber("Проверка номера страницы")
                    validateClientCardPageSize("Проверка размера страницы")
                    finishClientCardFilterValidation("Завершение валидации фильтра карточек")
                }
                clientCardRepoSearch("Поиск карточек клиентов в БД по фильтру")
                prepareRepoResult("Подготовка ответа")
            }

            // TrainingPlan Commands
            trainingPlanOperation("Создание тренировочного плана", TrainingPlanCommand.CREATE) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubValidationBadPlanTitle("Неверное название")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                trainingPlanValidation("Валидация создания тренировочного плана") {
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
                    trainingPlanRepoPrepareCreate("Подготовка объекта для сохранения")
                    trainingPlanRepoCreate("Создание тренировочного плана в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            trainingPlanOperation("Чтение тренировочного плана", TrainingPlanCommand.READ) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                trainingPlanValidation("Валидация чтения тренировочного плана") {
                    prepareTrainingPlanValidation("Подготовка тренировочного плана")
                    validateTrainingPlanIdNotEmpty("Проверка непустого ID")
                    validateTrainingPlanIdFormat("Проверка формата ID")
                    finishTrainingPlanValidation("Завершение валидации тренировочного плана")
                }
                chain {
                    title = "Логика чтения"
                    trainingPlanRepoRead("Чтение тренировочного плана из БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            trainingPlanOperation("Обновление тренировочного плана", TrainingPlanCommand.UPDATE) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubValidationBadPlanTitle("Неверное название")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                trainingPlanValidation("Валидация обновления тренировочного плана") {
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
                    trainingPlanRepoPrepareUpdate("Подготовка объекта для обновления")
                    trainingPlanRepoUpdate("Обновление тренировочного плана в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            trainingPlanOperation("Архивирование тренировочного плана", TrainingPlanCommand.ARCHIVE) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubCannotArchive("Невозможно архивировать")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                trainingPlanValidation("Валидация архивирования тренировочного плана") {
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
                    trainingPlanRepoPrepareArchive("Подготовка объекта для архивирования")
                    trainingPlanRepoArchive("Архивирование тренировочного плана в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            trainingPlanOperation("Поиск тренировочного плана", TrainingPlanCommand.SEARCH) {
                initTrainingPlanRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubTrainingPlanSuccess("Успешная обработка")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                trainingPlanValidation("Валидация поиска тренировочных планов") {
                    prepareTrainingPlanFilterValidation("Подготовка фильтра тренировочных планов")
                    validateTrainingPlanFilterClientCardIdFormat("Проверка ID карточки клиента")
                    validateTrainingPlanSearchStringLength("Проверка длины строки поиска")
                    validateTrainingPlanFilterStatus("Проверка статуса плана")
                    validateTrainingPlanPageNumber("Проверка номера страницы")
                    validateTrainingPlanPageSize("Проверка размера страницы")
                    finishTrainingPlanFilterValidation("Завершение валидации фильтра планов")
                }
                trainingPlanRepoSearch("Поиск тренировочных планов в БД по фильтру")
                prepareRepoResult("Подготовка ответа")
            }
        }.build()
}
