package com.epam.brn.integration.configuration

import com.epam.brn.config.AwsConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Configuration
@Testcontainers
class AwsTestConfig {
    companion object {
        const val BUCKET_NAME = "somebucket"

        @Container
        public var localStack =
            LocalStackContainer(DockerImageName.parse("localstack/localstack:0.14.3"))
                .withServices(LocalStackContainer.Service.S3)

        init {
            localStack.start()
        }
    }

    @Bean
    fun awsConfig(
        @Value("\${cloud.expireAfterDuration}") expireAfterDuration: String,
        @Value("\${aws.accessRuleCanned}") accessRuleCanned: String,
        @Value("\${aws.credentialsPath}") credentialsPath: String,
        @Value("\${aws.accessKeyId}") accessKeyIdProperty: String,
        @Value("\${aws.secretAccessKey}") secretAccessKeyProperty: String,
        @Value("\${aws.region}") region: String,
    ): AwsConfig =
        object : AwsConfig(expireAfterDuration, accessRuleCanned, credentialsPath, accessKeyIdProperty, secretAccessKeyProperty, region) {
            override fun instant(): OffsetDateTime = Instant.ofEpochMilli(1580384357114).atOffset(ZoneOffset.UTC)
            override fun uuid(): String = "c49791b2-b27b-4edf-bac8-8734164c20e6"
        }

    @Bean
    fun s3Client(): S3Client {
        val credentials =
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    localStack.accessKey,
                    localStack.secretKey,
                ),
            )

        val s3Client =
            S3Client
                .builder()
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(credentials)
                .region(Region.of(localStack.region))
                .build()

        s3Client.createBucket(
            CreateBucketRequest
                .builder()
                .bucket(BUCKET_NAME)
                .build(),
        )
        return s3Client
    }
}
