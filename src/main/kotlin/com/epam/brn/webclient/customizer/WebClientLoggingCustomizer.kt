package com.epam.brn.webclient.customizer

import org.apache.logging.log4j.kotlin.logger
import org.reactivestreams.Publisher
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.reactive.ClientHttpRequestDecorator
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Customize WebClient to make logging request and response
 */
class WebClientLoggingCustomizer : WebClientCustomizer {
    private val log = logger()

    override fun customize(webClientBuilder: WebClient.Builder?) {
        webClientBuilder!!.filter { request, next ->
            logRequest(request)
            next
                .exchange(interceptRequestBody(request))
                .doOnNext(this::logResponse)
                .map(this::interceptResponseBody)
        }
    }

    /**
     * Catcher request body
     *
     * @param request request
     * @return request
     */
    private fun interceptRequestBody(request: ClientRequest): ClientRequest =
        ClientRequest
            .from(request)
            .body { outputMessage, context ->
                val newOutputMessage =
                    object : ClientHttpRequestDecorator(outputMessage) {
                        override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> =
                            super.writeWith(
                                Flux
                                    .from(body)
                                    .doOnNext { logRequestBody(it) },
                            )
                    }
                request.body().insert(newOutputMessage, context)
            }.build()

    /**
     * Catcher response body
     *
     * @param response response
     * @return response
     */
    private fun interceptResponseBody(response: ClientResponse): ClientResponse? =
        response
            .mutate()
            .body { data -> data.doOnNext { dataBuffer: DataBuffer -> logResponseBody(dataBuffer) } }
            .build()

    /**
     * Log request
     *
     * @param request request
     */
    private fun logRequest(request: ClientRequest) {
        log.debug("Request: method=${request.method()}, url=${request.url()}, headers=${request.headers()}")
    }

    /**
     * Log request body
     *
     * @param dataBuffer buffer of body request
     */
    private fun logRequestBody(dataBuffer: DataBuffer) {
        log.trace("Request: body=$dataBuffer")
    }

    /**
     * Log response
     *
     * @param response ответ
     */
    private fun logResponse(response: ClientResponse) {
        log.debug("Response: status=${response.rawStatusCode()}, headers=${response.headers().asHttpHeaders()}")
    }

    /**
     * Log response body
     *
     * @param dataBuffer buffer of body response
     */
    private fun logResponseBody(dataBuffer: DataBuffer) {
        log.trace("Response: body=$dataBuffer")
    }
}
