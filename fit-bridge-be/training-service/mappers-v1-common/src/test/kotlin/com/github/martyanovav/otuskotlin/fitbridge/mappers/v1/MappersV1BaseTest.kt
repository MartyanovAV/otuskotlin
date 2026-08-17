package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import kotlin.test.Test
import kotlin.test.assertEquals

class MappersV1BaseTest {
    @Test
    fun `all spec debug stubs map to domain stub cases`() {
        val cases =
            listOf(
                RequestDebugStubs.SUCCESS to Stubs.SUCCESS,
                RequestDebugStubs.NOT_FOUND to Stubs.NOT_FOUND,
                RequestDebugStubs.BAD_ID to Stubs.BAD_ID,
                RequestDebugStubs.BAD_LOCK to Stubs.BAD_LOCK,
                RequestDebugStubs.BAD_PUBLIC_NAME to Stubs.BAD_PUBLIC_NAME,
                RequestDebugStubs.BAD_CLIENT_NAME to Stubs.BAD_CLIENT_NAME,
                RequestDebugStubs.BAD_PLAN_TITLE to Stubs.BAD_PLAN_TITLE,
                RequestDebugStubs.BAD_PLAN_BODY to Stubs.BAD_PLAN_BODY,
                RequestDebugStubs.FORBIDDEN to Stubs.FORBIDDEN,
                RequestDebugStubs.VALIDATION_ERROR to Stubs.VALIDATION_ERROR,
                RequestDebugStubs.CANNOT_ARCHIVE to Stubs.CANNOT_ARCHIVE,
            )

        assertEquals(RequestDebugStubs.entries.size, cases.size, "Spec stub enum changed, update the test cases")
        cases.forEach { (transportStub, expected) ->
            val actual = Debug(mode = RequestDebugMode.STUB, stub = transportStub).transportToStubCase()
            assertEquals(expected, actual, "Unexpected domain stub for transport stub $transportStub")
        }
    }

    @Test
    fun `missing debug stub maps to none`() {
        val debugWithoutStub: Debug = Debug(mode = RequestDebugMode.STUB)
        val nullDebug: Debug? = null

        assertEquals(Stubs.NONE, debugWithoutStub.transportToStubCase())
        assertEquals(Stubs.NONE, nullDebug.transportToStubCase())
    }
}
