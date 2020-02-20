package com.epam.brn.controller.advice

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.exception.FileFormatException
import java.io.IOException
import java.lang.IllegalArgumentException
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionControllerAdvice {

    private val logger = logger()

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException): ResponseEntity<BaseResponseDto> {
        logger.error("Entity not found exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(FileFormatException::class)
    fun handleFileFormatException(e: FileFormatException): ResponseEntity<BaseResponseDto> {
        logger.error("File format exception: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<BaseResponseDto> {
        logger.error("IllegalArgumentException: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BaseResponseDto(errors = listOf(e.message.toString())))
    }

    @ExceptionHandler(UninitializedPropertyAccessException::class)
    fun handleUninitializedPropertyAccessException(e: Throwable): ResponseEntity<BaseResponseDto> {
        logger.error("Internal exception: ${e.message}", e)
        return makeInternalServerErrorResponseEntity(e)
    }

    @ExceptionHandler(IOException::class)
    fun handleIOException(e: IOException): ResponseEntity<BaseResponseDto> {
        logger.error("Internal exception: ${e.message}", e)
        return makeInternalServerErrorResponseEntity(e)
    }

    @ExceptionHandler(Throwable::class)
    fun handleException(e: Throwable): ResponseEntity<BaseResponseDto> {
        logger.error("Internal exception: ${e.message}", e)
        return makeInternalServerErrorResponseEntity(e)
    }

    fun makeInternalServerErrorResponseEntity(e: Throwable) = ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BaseResponseDto(errors = listOf(e.message.toString())))
}
