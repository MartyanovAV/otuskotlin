package com.github.martyanovav.otuskotlin.fitbridge.training.common

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSession
import kotlin.time.Instant

data class ClientCardContext(
    override var command: FBCommand = ClientCardCommand.NONE,
    override var state: State = State.NONE,
    override val errors: MutableList<FBError> = mutableListOf(),
    override var workMode: WorkMode = WorkMode.PROD,
    override var stubCase: Stubs = Stubs.NONE,
    override var requestId: RequestId = RequestId.NONE,
    override var timeStart: Instant = Instant.DISTANT_PAST,
    override var principal: AuthPrincipal = AuthPrincipal.NONE,
    override var wsSession: IFBWsSession = IFBWsSession.NONE,
    override var corSettings: CorSettings = CorSettings(),
    var clientCardRequest: ClientCard = ClientCard(),
    var clientCardValidating: ClientCard = ClientCard(),
    var clientCardValidated: ClientCard = ClientCard(),
    var clientCardResponse: ClientCard = ClientCard(),
    var clientCardsResponse: Page<ClientCard> = Page(),
    var clientCardFilter: ClientCardFilter = ClientCardFilter(),
    var clientCardFilterValidating: ClientCardFilter = ClientCardFilter(),
    var clientCardFilterValidated: ClientCardFilter = ClientCardFilter(),
    var clientCardRepo: IRepoClientCard = IRepoClientCard.NONE,
    var clientCardRepoRead: ClientCard = ClientCard(),
    var clientCardRepoPrepare: ClientCard = ClientCard(),
    var clientCardRepoDone: ClientCard = ClientCard(),
    var clientCardsRepoDone: MutableList<ClientCard> = mutableListOf(),
) : IFBContext {
    override fun addError(error: FBError) {
        errors.add(error)
    }
}
