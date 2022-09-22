package com.epam.brn.controller.advice

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.exception.FileFormatException
import com.epam.brn.upload.csv.CsvParser
import org.apache.logging.log4j.kotlin.logger
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.IOException

@ControllerAdvice
@PropertySource("classpath:errorMessages.properties")
class ExceptionControllerAdvice {

    private val logger = logger()

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException): ResponseEntity<BaseResponse> {
        logger.error("Entity not found exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(FileFormatException::class)
    fun handleFileFormatException(e: FileFormatException): ResponseEntity<BaseResponse> {
        logger.error("File format exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(CsvParser.ParseException::class)
    fun handleCsvFileParseException(e: CsvParser.ParseException): ResponseEntity<BaseResponse> {
        logger.error("Csv file parsing exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = e.errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<BaseResponse> {
        logger.error("IllegalArgumentException: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<BaseResponse> {
        logger.error("Forbidden: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(UninitializedPropertyAccessException::class)
    fun handleUninitializedPropertyAccessException(e: Throwable): ResponseEntity<BaseResponse> {
        return createInternalErrorResponse(e)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        e: HttpMessageNotReadableException
    ): ResponseEntity<BaseResponse> {
        logger.error("Argument Validation Error: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<BaseResponse> {
        logger.error("Argument Validation Error: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = processValidationErrors(e.bindingResult.fieldErrors)))
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDeniedException(e: org.springframework.security.access.AccessDeniedException): ResponseEntity<BaseResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = listOf(e.message.toString())))
    }

    private fun processValidationErrors(fieldErrors: List<FieldError>): List<String> {
        return fieldErrors.mapNotNull { fieldError -> fieldError.defaultMessage }.toList()
    }

    @ExceptionHandler(IOException::class)
    fun handleIOException(e: IOException): ResponseEntity<BaseResponse> {
        return createInternalErrorResponse(e)
    }

    @ExceptionHandler(Throwable::class)
    fun handleException(e: Throwable): ResponseEntity<BaseResponse> {
        return createInternalErrorResponse(e)
    }

    fun createInternalErrorResponse(e: Throwable): ResponseEntity<BaseResponse> {
        logger.error("Internal exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponse(errors = listOf(e.message.toString())))
    }
}
