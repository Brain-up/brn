package com.epam.brn.controller.advice

import com.epam.brn.exception.NoDataFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

internal class ExceptionControllerAdviceTest {
    private val exceptionControllerAdvice: ExceptionControllerAdvice =
        ExceptionControllerAdvice()

    @Test
    fun `should handle NoDataFoundException`() {
        // GIVEN
        val exception = NoDataFoundException("tasks were not found")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleNoDataFoundException(exception)
        // THEN
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON_UTF8, responseEntity.getHeaders().getContentType())
    }

    @Test
    fun `should handle Exception`() {
        // GIVEN
        val exception = Exception("some exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleException(exception)
        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON_UTF8, responseEntity.getHeaders().getContentType())
    }

    @Test
    fun `should make InternalServerErrorResponseEntity`() {
        // GIVEN
        val exception = Exception("some test exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.makeInternalServerErrorResponseEntity(exception)
        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON_UTF8, responseEntity.getHeaders().getContentType())
    }
}