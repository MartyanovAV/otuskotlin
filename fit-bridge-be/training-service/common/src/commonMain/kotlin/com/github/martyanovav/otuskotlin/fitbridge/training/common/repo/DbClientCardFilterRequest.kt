package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardStatus

data class DbClientCardFilterRequest(
    val searchString: String = "",
    val status: ClientCardStatus = ClientCardStatus.NONE,
    val pageNumber: Int = 1,
    val pageSize: Int = 10,
)
