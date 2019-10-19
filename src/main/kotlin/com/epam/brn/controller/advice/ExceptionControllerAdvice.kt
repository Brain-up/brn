package com.epam.brn.controller.advice

import com.epam.brn.dto.ErrorResponse
import com.epam.brn.exception.NoDataFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.apache.logging.log4j.kotlin.logger
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionControllerAdvice {

    private val logger = logger()

    @ExceptionHandler(NoDataFoundException::class)
    fun handleNoDataFoundException(e: NoDataFoundException): ResponseEntity<ErrorResponse> {
        logger.error("Data was not found. ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(ErrorResponse(e.message))
    }

    @ExceptionHandler(Throwable::class)
    fun handleException(e: Throwable): ResponseEntity<ErrorResponse> {
        logger.error("Internal exception: ${e.message}", e)
        return makeInternalServerErrorResponseEntity(e)
    }

    fun makeInternalServerErrorResponseEntity(e: Throwable) = ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .body(ErrorResponse(e.message))
}