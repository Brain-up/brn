package com.epam.brn.config

import org.keycloak.OAuth2Constants
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakCustomConfig {

    @Bean
    fun keycloak(props: KeycloakSpringBootProperties): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(props.authServerUrl)
            .realm(props.realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(props.resource)
            .clientSecret(props.credentials["secret"] as String?)
            .build()
    }
}
