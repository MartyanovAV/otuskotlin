package com.github.martyanovav.otuskotlin.fitbridge.training.biz

import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.cor.rootChain
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.access.accessValidationClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.access.initClientCardUserIds
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.access.resolveClientCardRelation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.initStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.operation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.general.stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoCreate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoPrepareArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoPrepareCreate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoPrepareUpdate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoRead
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoSearch
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.clientCardRepoUpdate
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.initClientCardRepo
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo.prepareRepoResult
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubCannotArchive
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubClientCardSuccess
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubNoCase
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubNotFound
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs.stubValidationBadId
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.finishClientCardFilterValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.finishClientCardValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.prepareClientCardFilterValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.prepareClientCardValidation
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
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation.validation
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardCorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand

class ClientCardProcessor(
    private val settings: ClientCardCorSettings
) {
    suspend fun exec(ctx: ClientCardContext) {
        ctx.corSettings = settings
        businessChain.exec(ctx)
    }

    private val businessChain =
        rootChain<ClientCardContext> {
            initStatus("Инициализация статуса")

            // ClientCard Commands
            operation("Создание карточки клиента", ClientCardCommand.CREATE) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация создания карточки клиента") {
                    prepareClientCardValidation("Подготовка карточки клиента", resetIdentity = true)
                    validateClientCardDisplayNameNotEmpty("Проверка непустого имени клиента")
                    validateClientCardDisplayNameMaxLength("Проверка длины имени клиента")
                    validateClientCardDisplayNameHasContent("Проверка содержимого имени клиента")
                    validateClientCardNoteMaxLength("Проверка длины заметки")
                    finishClientCardValidation("Завершение валидации карточки клиента")
                }
                chain {
                    title = "Логика сохранения"
                    resolveClientCardRelation("Вычисление прав доступа")
                    accessValidationClientCard("Проверка прав доступа")
                    initClientCardUserIds("Установка владельца и автора")
                    clientCardRepoPrepareCreate("Подготовка объекта для сохранения")
                    clientCardRepoCreate("Создание карточки клиента в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Чтение карточки клиента", ClientCardCommand.READ) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация чтения карточки клиента") {
                    prepareClientCardValidation("Подготовка карточки клиента")
                    validateClientCardIdNotEmpty("Проверка непустого ID")
                    validateClientCardIdFormat("Проверка формата ID")
                    finishClientCardValidation("Завершение валидации карточки клиента")
                }
                chain {
                    title = "Логика чтения"
                    clientCardRepoRead("Чтение карточки клиента из БД")
                    resolveClientCardRelation("Вычисление прав доступа")
                    accessValidationClientCard("Проверка прав доступа")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Обновление карточки клиента", ClientCardCommand.UPDATE) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация обновления карточки клиента") {
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
                    resolveClientCardRelation("Вычисление прав доступа")
                    accessValidationClientCard("Проверка прав доступа")
                    clientCardRepoPrepareUpdate("Подготовка объекта для обновления")
                    clientCardRepoUpdate("Обновление карточки клиента в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Архивирование карточки клиента", ClientCardCommand.ARCHIVE) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNotFound("Не найдено")
                    stubValidationBadId("Неверный ID")
                    stubCannotArchive("Невозможно архивировать")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация архивирования карточки клиента") {
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
                    resolveClientCardRelation("Вычисление прав доступа")
                    accessValidationClientCard("Проверка прав доступа")
                    clientCardRepoPrepareArchive("Подготовка объекта для архивирования")
                    clientCardRepoArchive("Архивирование карточки клиента в БД")
                }
                prepareRepoResult("Подготовка ответа")
            }
            operation("Поиск карточки клиента", ClientCardCommand.SEARCH) {
                initClientCardRepo("Инициализация репозитория")
                stubs("Обработка стабов") {
                    stubClientCardSuccess("Успешная обработка")
                    stubNoCase("Ошибка: запрошенный стаб недопустим")
                }
                validation("Валидация поиска карточек клиентов") {
                    prepareClientCardFilterValidation("Подготовка фильтра карточек клиентов")
                    validateClientCardSearchStringLength("Проверка длины строки поиска")
                    validateClientCardFilterStatus("Проверка статуса карточки")
                    validateClientCardPageNumber("Проверка номера страницы")
                    validateClientCardPageSize("Проверка размера страницы")
                    finishClientCardFilterValidation("Завершение валидации фильтра карточек")
                }
                resolveClientCardRelation("Вычисление прав доступа")
                accessValidationClientCard("Проверка прав доступа")
                clientCardRepoSearch("Поиск карточек клиентов в БД по фильтру")
                prepareRepoResult("Подготовка ответа")
            }
        }.build()
}
