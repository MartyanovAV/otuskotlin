package com.github.martyanovav.otuskotlin.fitbridge.profile.common.models

data class Page<T>(
    val items: List<T> = emptyList(),
    val totalSize: Int = 0,
    val pageNumber: Int = 1,
    val pageSize: Int = 10,
)
