package com.epam.brn.integration.configuration

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.FileInputStream

@Configuration
@Testcontainers
class FirebaseTestConfig(
    @Value("\${firebase.test.config.path}") private val firebaseTestConfigPath: String,
) {
    @Bean
    fun firebaseApp(): FirebaseApp {
        if (FirebaseApp.getApps().isEmpty()) {
            val refreshToken = FileInputStream(firebaseTestConfigPath)
            val options =
                FirebaseOptions
                    .builder()
                    .setCredentials(GoogleCredentials.fromStream(refreshToken))
                    .build()
            FirebaseApp.initializeApp(options)
        }
        return FirebaseApp.getInstance()
    }

    @Bean
    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance(firebaseApp())

    @Bean
    fun firebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance(firebaseApp())
}
