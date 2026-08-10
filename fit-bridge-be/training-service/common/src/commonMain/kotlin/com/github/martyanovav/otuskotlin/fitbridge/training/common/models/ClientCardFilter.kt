package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

data class ClientCardFilter(
    var status: String = "",
    var searchString: String = "",
    var pageNumber: Int = 1,
    var pageSize: Int = 10,
) {
    fun deepCopy(): ClientCardFilter = copy()
}
