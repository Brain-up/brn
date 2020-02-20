package com.epam.brn.controller.advice

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.exception.FileFormatException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

internal class ExceptionControllerAdviceTest {
    private val exceptionControllerAdvice: ExceptionControllerAdvice =
        ExceptionControllerAdvice()

    @Test
    fun `should handle EntityNotFoundException`() {
        // GIVEN
        val exception = EntityNotFoundException("tasks were not found")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleEntityNotFoundException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("tasks were not found"))
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType())
    }

    @Test
    fun `should handle Exception`() {
        // GIVEN
        val exception = Exception("some exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("some exception"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType())
    }

    @Test
    fun `should handle FileFormatException`() {
        // GIVEN
        val exception = FileFormatException(CSV_FILE_FORMAT_ERROR)
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleFileFormatException(exception)
        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
        assertTrue((responseEntity.body as BaseResponseDto).errors.contains(CSV_FILE_FORMAT_ERROR))
    }

    @Test
    fun `should make InternalServerErrorResponseEntity`() {
        // GIVEN
        val exception = Exception("some test exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.makeInternalServerErrorResponseEntity(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("some test exception"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType())
        }
}
