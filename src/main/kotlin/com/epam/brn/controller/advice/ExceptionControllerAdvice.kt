package com.epam.brn.controller.advice

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.exception.FileFormatException
import com.epam.brn.upload.csv.CsvParser
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.IOException

@ControllerAdvice
class ExceptionControllerAdvice {

    private val logger = logger()

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException): ResponseEntity<BaseResponseDto> {
        logger.warn("Entity not found exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(FileFormatException::class)
    fun handleFileFormatException(e: FileFormatException): ResponseEntity<BaseResponseDto> {
        logger.warn("File format exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(CsvParser.ParseException::class)
    fun handleCsvFileParseException(e: CsvParser.ParseException): ResponseEntity<BaseResponseDto> {
        logger.warn("Csv file parsing exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = e.errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<BaseResponseDto> {
        logger.warn("IllegalArgumentException: ${e.message}", e)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<BaseResponseDto> {
        logger.warn("Forbidden: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(UninitializedPropertyAccessException::class)
    fun handleUninitializedPropertyAccessException(e: Throwable): ResponseEntity<BaseResponseDto> {
        return createInternalErrorResponse(e)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<BaseResponseDto> {
        logger.warn("Argument Validation Error: ${e.message}", e)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = processValidationErrors(e.bindingResult.fieldErrors)))
    }

    // @ExceptionHandler(InvalidFormatException::class)
    // fun handleInvalidFormatException(e: InvalidFormatException): ResponseEntity<BaseResponseDto> {
    //     logger.warn("Argument Validation Error: ${e.message}", e)
    //
    //     return ResponseEntity
    //         .status(HttpStatus.BAD_REQUEST)
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .body(BaseResponseDto(errors = e.localizedMessage.toString().toList()))
    // }
    //
    // @ExceptionHandler(HttpMessageNotReadableException::class)
    // fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<BaseResponseDto> {
    //     logger.warn("Argument Validation Error: ${e.message}", e)
    //
    //     return ResponseEntity
    //         .status(HttpStatus.BAD_REQUEST)
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .body(BaseResponseDto(errors = e.localizedMessage.toString().toList()))
    // }

    private fun processValidationErrors(fieldErrors: List<FieldError>): List<String> {
        return fieldErrors.mapNotNull { fieldError -> fieldError.defaultMessage }.toList()
    }

    @ExceptionHandler(IOException::class)
    fun handleIOException(e: IOException): ResponseEntity<BaseResponseDto> {
        return createInternalErrorResponse(e)
    }

    @ExceptionHandler(Throwable::class)
    fun handleException(e: Throwable): ResponseEntity<BaseResponseDto> {
        return createInternalErrorResponse(e)
    }

    fun createInternalErrorResponse(e: Throwable): ResponseEntity<BaseResponseDto> {
        logger.error("Internal exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = listOf(e.message.toString())))
    }
}
