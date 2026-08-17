@file:Suppress("unused")

package com.github.martyanovav.otuskotlin.fitbridge.api.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
@Suppress("JSON_FORMAT_REDUNDANT_DEFAULT")
val apiV2Mapper =
    Json {
//    ignoreUnknownKeys = true
        allowTrailingComma = true
    }

@Suppress("UNCHECKED_CAST")
fun <T : IRequest> apiV2RequestDeserialize(json: String) =
    apiV2Mapper.decodeFromString<IRequest>(json) as T

fun apiV2ResponseSerialize(obj: IResponse): String =
    apiV2Mapper.encodeToString(IResponse.serializer(), obj)

@Suppress("UNCHECKED_CAST")
fun <T : IResponse> apiV2ResponseDeserialize(json: String) =
    apiV2Mapper.decodeFromString<IResponse>(json) as T

inline fun <reified T : IResponse> apiV2ResponseSimpleDeserialize(json: String) =
    apiV2Mapper.decodeFromString<T>(json)

@Suppress("unused")
fun apiV2RequestSerialize(obj: IRequest): String =
    apiV2Mapper.encodeToString(IRequest.serializer(), obj)

@Suppress("unused")
inline fun <reified T : IRequest> apiV2RequestSimpleSerialize(obj: T): String =
    apiV2Mapper.encodeToString<T>(obj)
