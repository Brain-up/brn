package com.epam.brn.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.common.collect.Lists
import java.io.FileInputStream
import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class GoogleCloudConfig {

    @Value("\${cloud.expireAfterDuration}")
    val expireAfterDuration: String = ""
    @Value("\${google.credentialsPath}")
    val credentialsPath: String = ""
    @Value("\${google.projectId}")
    val projectId: String = ""
    @Value("\${google.bucketName}")
    val bucketName: String = ""
    @Value("\${google.bucketLink}")
    val bucketLink: String = ""
    @Value("\${google.credentialScope}")
    val credentialScope: String = ""

    val expireAfter: Duration by lazy { Duration.parse(expireAfterDuration) }
    val credentials: GoogleCredentials by lazy { GoogleCredentials.fromStream(FileInputStream(credentialsPath))
        .createScoped(Lists.newArrayList(credentialScope)) }
}
