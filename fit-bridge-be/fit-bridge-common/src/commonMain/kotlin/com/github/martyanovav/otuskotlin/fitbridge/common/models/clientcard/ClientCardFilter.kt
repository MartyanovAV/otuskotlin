package com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard

data class ClientCardFilter(
    var searchString: String = "",
    var pageNumber: Int = 1,
    var pageSize: Int = 10,
)
