package com.epam.brn.integration.firebase

import com.epam.brn.integration.firebase.model.FirebaseVerifyPasswordRequest
import com.epam.brn.integration.firebase.model.FirebaseVerifyPasswordResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient

@Service
class FirebaseWebClient {

    private val webClient: WebClient

    @Value("\${webclient.firebase.path.verify-password}")
    private var verifyPasswordPath: String = ""

    @Value("#{\${webclient.firebase.verify-password.query}}")
    private lateinit var verifyPasswordQuery: MultiValueMap<String, String>

    constructor(@Value("\${webclient.firebase.url}") url: String) {
        webClient = WebClient.builder()
            .baseUrl(url)
            .build()
    }

    fun verifyPassword(request: FirebaseVerifyPasswordRequest): FirebaseVerifyPasswordResponse? {
        return webClient.post()
            .uri { uriBuilder ->
                uriBuilder.path(verifyPasswordPath)
                    .queryParams(verifyPasswordQuery).build()
            }
            .bodyValue(request)
            .exchangeToMono { response -> response.bodyToMono(FirebaseVerifyPasswordResponse::class.java) }
            .block()
    }
}
