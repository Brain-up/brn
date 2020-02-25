package com.epam.brn.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.glacier.model.CannedACL
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import java.io.FileInputStream
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Properties
import java.util.UUID
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!integration-tests")
class AwsConfig {
    private val log = logger()

    companion object {
        val dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")!!
        val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")!!
        val expirationFormat = DateTimeFormatter.ISO_DATE_TIME!!
    }

    @Value("\${cloud.expireAfterDuration}")
    val expireAfterDuration: String = ""
    @Value("\${aws.credentialsPath}")
    val credentialsPath: String = ""
    @Value("\${aws.accessRuleCanned}")
    val accessRuleCanned: String = ""
    val credentials: Properties by lazy { initCredentials() }
    val accessKeyId: String by lazy { credentials.getProperty("aws.accessKeyId", "") }
    @Value("\${aws.bucketName}")
    val bucketName: String = ""
    @Value("\${aws.xamzCredential}")
    val xamzCredential: String = ""
    // optional
    @Value("\${aws.successActionRedirect:}")
    val successActionRedirect: String = ""
    @Value("\${aws.contentTypeStartsWith:}")
    val contentTypeStartsWith: String = ""
    @Value("\${aws.metaTagStartsWith:}")
    val metaTagStartsWith: String = ""
    // signature calc
    val secretAccessKey: String by lazy { credentials.getProperty("aws.secretAccessKey", "") }
    @Value("\${aws.serviceName}")
    val serviceName: String = ""
    @Value("\${aws.region}")
    val region: String = ""

    @Value("\${aws.bucketLink}")
    val bucketLink: String = ""

    fun instant(): OffsetDateTime = Instant.now().atOffset(ZoneOffset.UTC)
    fun uuid(): String = UUID.randomUUID().toString()
    fun initCredentials(): Properties {
        val properties = Properties()
        try {
            FileInputStream(credentialsPath).use { input ->
                properties.load(input)
            }
        } catch (ex: IOException) {
            log.info("Could not load aws properties from path $credentialsPath")
        }
        return properties
    }

    fun getAmazonS3(): AmazonS3 {
        val credentials = AWSStaticCredentialsProvider(
            BasicAWSCredentials(
                this.accessKeyId,
                this.secretAccessKey
            )
        )
        return AmazonS3ClientBuilder.standard()
            .withCredentials(credentials)
            .withRegion(this.region)
            .build()
    }
    fun accessRule() = CannedACL.valueOf(accessRuleCanned).toString()
    fun expireAfter() = Duration.parse(expireAfterDuration)
    private fun expiration(dateTime: OffsetDateTime): String =
        expirationFormat.format(dateTime.plus(expireAfter()))
    private fun dateTimeFormat(dateTime: OffsetDateTime): String = dateTimeFormat.format(dateTime)
    private fun dateFormat(dateTime: OffsetDateTime): String = dateFormat.format(dateTime)
    private fun credentialFormat(dateTime: OffsetDateTime): String = String.format(xamzCredential, this@AwsConfig.accessKeyId, this.dateFormat(dateTime))

    fun getConditions(filePath: String): Conditions {
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty())
            throw UninitializedPropertyAccessException("Missing property in cloud upload configuration")
        return this.Conditions(instant(), uuid(), filePath)
    }

    inner class Conditions constructor(now: OffsetDateTime, uuidString: String, filePath: String) {
        val date: String = dateFormat(now)

        // POLICY CONDITIONS
        val bucket: Pair<String, String> = "bucket" to bucketName
        val acl: Pair<String, String> = "acl" to accessRule()
        val uuid: Pair<String, String> = "x-amz-meta-uuid" to uuidString
        val serverSideEncryption: Pair<String, String> = "x-amz-server-side-encryption" to "AES256"
        val credential: Pair<String, String> = "x-amz-credential" to credentialFormat(now)
        val algorithm: Pair<String, String> = "x-amz-algorithm" to "AWS4-HMAC-SHA256"
        val dateTime: Pair<String, String> = "x-amz-date" to dateTimeFormat(now)
        val expiration: Pair<String, String> = "expiration" to expiration(now)
        val uploadKey: Pair<String, String> = "key" to filePath
        val successActionRedirect: Pair<String, String> = "success_action_redirect" to this@AwsConfig.successActionRedirect
        val contentTypeStartsWith: Pair<String, String> = "Content-Type" to this@AwsConfig.contentTypeStartsWith
        val metaTagStartsWith: Pair<String, String> = "x-amz-meta-tag" to this@AwsConfig.metaTagStartsWith

        override fun toString(): String {
            return "Conditions(date='$date', bucket=$bucket, acl=$acl, uuid=$uuid, serverSideEncryption=$serverSideEncryption, credential=$credential, algorithm=$algorithm, dateTime=$dateTime, expiration=$expiration, uploadKey=$uploadKey, successActionRedirect=$successActionRedirect, contentTypeStartsWith=$contentTypeStartsWith, metaTagStartsWith=$metaTagStartsWith, uploadKey=$uploadKey)"
        }
    }
}
