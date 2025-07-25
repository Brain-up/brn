package com.epam.brn.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.common.collect.Lists
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.time.Duration

@ConditionalOnProperty(name = ["cloud.provider"], havingValue = "google")
@Configuration
class GoogleCloudConfig(
    @Value("\${google.credentialsPath}") credentialsPath: String,
    @Value("\${google.projectId}") projectId: String,
    @Value("\${google.credentialScope}") credentialScope: String,
    @Value("\${cloud.expireAfterDuration}") expireAfterDuration: String,
) {
    @Value("\${google.bucketName}")
    val bucketName: String = ""

    @Value("\${google.bucketLink}")
    val bucketLink: String = ""

    final var expireAfter: Duration
    final var storage: Storage?

    init {
        val credentials =
            GoogleCredentials
                .fromStream(FileInputStream(credentialsPath))
                .createScoped(Lists.newArrayList(credentialScope))
        storage =
            StorageOptions
                .newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .service
        expireAfter = Duration.parse(expireAfterDuration)
    }
}
