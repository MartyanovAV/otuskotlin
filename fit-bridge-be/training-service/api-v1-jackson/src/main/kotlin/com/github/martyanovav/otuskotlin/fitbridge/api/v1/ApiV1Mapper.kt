package com.github.martyanovav.otuskotlin.fitbridge.api.v1

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IResponse

val apiV1Mapper =
    JsonMapper.builder().run {
        addModule(KotlinModule.Builder().build())
        addMixIn(IResponse::class.java, IResponseSerializationMixin::class.java)
        enable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
        build()
    }

private abstract class IResponseSerializationMixin {
    @get:JsonIgnore
    abstract val responseType: String?
}

@Suppress("unused")
fun apiV1RequestSerialize(request: IRequest): String = apiV1Mapper.writeValueAsString(request)

@Suppress("UNCHECKED_CAST", "unused")
fun <T : IRequest> apiV1RequestDeserialize(json: String): T = apiV1Mapper.readValue(json, IRequest::class.java) as T

@Suppress("unused")
fun apiV1ResponseSerialize(response: IResponse): String = apiV1Mapper.writeValueAsString(response)

@Suppress("UNCHECKED_CAST", "unused")
fun <T : IResponse> apiV1ResponseDeserialize(json: String): T = apiV1Mapper.readValue(json, IResponse::class.java) as T
