package com.epam.brn.controller.advice

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.exception.FileFormatException
import com.epam.brn.upload.csv.CsvParser
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.PropertySource
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.MockHttpInputMessage
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import java.io.IOException
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeParseException
import kotlin.test.assertNotNull

@PropertySource("classpath:errorMessages.properties")
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
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle MethodArgumentNotValidException`() {

        // GIVEN
        val bindingResult = mockk<BindingResult>()
        val method = mockk<Method>()
        every { method.parameterCount } answers { 2 }
        every { method.toGenericString() } answers { "" }
        val methodParameter = MethodParameter(method, -1)
        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)
        val fieldErrors: List<FieldError> = listOf(
            FieldError("TestEntity", "field1", "INCORRECT_FIELD_FORMAT"),
            FieldError("TestEntity", "firstName", "FIRST_NAME_MUST_NOT_HAVE_SPACES")
        )
        every { bindingResult.fieldErrors } answers { fieldErrors }
        every { bindingResult.errorCount } answers { fieldErrors.size }
        every { bindingResult.allErrors } answers { fieldErrors }
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleMethodArgumentNotValidException(exception)

        // THEN
        assertTrue(
            (responseEntity.body as BaseResponseDto).errors.containsAll(
                listOf(
                    "INCORRECT_FIELD_FORMAT",
                    "FIRST_NAME_MUST_NOT_HAVE_SPACES"
                )
            )
        )
        assertNotNull((responseEntity.body as BaseResponseDto).errors)
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle CsvFileParseException`() {
        // GIVEN
        val exception = CsvParser.ParseException(listOf("Csv file parsing exception"))
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleCsvFileParseException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("Csv file parsing exception"))
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle IllegalArgumentException`() {
        // GIVEN
        val exception = IllegalArgumentException("IllegalArgumentException")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleIllegalArgumentException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("IllegalArgumentException"))
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle BadCredentialsException`() {
        // GIVEN
        val exception = BadCredentialsException("Forbidden")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleBadCredentialsException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("Forbidden"))
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle UninitializedPropertyAccessException`() {
        // GIVEN
        val exception = UninitializedPropertyAccessException("some exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleUninitializedPropertyAccessException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("some exception"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle IOException`() {
        // GIVEN
        val exception = IOException("some exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleIOException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("some exception"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
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
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle FileFormatException`() {
        // GIVEN
        val exception = FileFormatException()
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleFileFormatException(exception)
        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
        assertTrue(
            (responseEntity.body as BaseResponseDto).errors
                .contains("Formatting error. Please upload file with csv extension.")
        )
    }

    @Test
    fun `should make InternalServerErrorResponseEntity`() {
        // GIVEN
        val exception = Exception("some test exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.createInternalErrorResponse(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("some test exception"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle HttpMessageNotReadableException`() {
        // GIVEN
        val body = "name+1=value+1&name+2=value+2%2B1&name+2=value+2%2B2&name+3"
        val inputMessage = MockHttpInputMessage(body.byteInputStream(StandardCharsets.ISO_8859_1))
        val exception = HttpMessageNotReadableException("TEST", DateTimeParseException("TEST", "TEST", 3), inputMessage)
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleHttpMessageNotReadableException(exception)
        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }
}
