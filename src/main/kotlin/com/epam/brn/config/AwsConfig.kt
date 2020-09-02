package com.epam.brn.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.glacier.model.CannedACL
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.FileInputStream
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Properties
import java.util.UUID

@Configuration
@Profile("!integration-tests")
@ConditionalOnProperty(name = ["cloud.provider"], havingValue = "aws")
class AwsConfig(
    @Value("\${cloud.expireAfterDuration}") var expireAfterDuration: String,
    @Value("\${aws.accessRuleCanned}") var accessRuleCanned: String,
    @Value("\${aws.credentialsPath:}") credentialsPath: String,
    @Value("\${aws.accessKeyId:}") accessKeyIdProperty: String,
    @Value("\${aws.secretAccessKey:}") secretAccessKeyProperty: String,
    @Value("\${aws.region}") val region: String
) {
    private val log = logger()

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
    @Value("\${aws.serviceName}")
    val serviceName: String = ""

    @Value("\${aws.bucketLink}")
    val bucketLink: String = ""

    @Value("\${aws.baseFileUrl}")
    val baseFileUrl: String = ""

    fun instant(): OffsetDateTime = Instant.now().atOffset(ZoneOffset.UTC)
    fun uuid(): String = UUID.randomUUID().toString()

    lateinit var accessKeyId: String
    lateinit var secretAccessKey: String
    lateinit var amazonS3: AmazonS3

    init {
        accessKeyId = accessKeyIdProperty
        secretAccessKey = secretAccessKeyProperty
        if (accessKeyId.isNullOrEmpty() || secretAccessKey.isNullOrEmpty()) {
            try {
                val credentials = Properties()
                FileInputStream(credentialsPath).use { input ->
                    credentials.load(input)
                }
                accessKeyId = credentials.getProperty("aws.accessKeyId")
                secretAccessKey = credentials.getProperty("aws.secretAccessKey")
            } catch (ex: IOException) {
                log.info("Could not load aws properties from path $credentialsPath")
            }
        }

        val credentials = AWSStaticCredentialsProvider(
            BasicAWSCredentials(
                accessKeyId,
                secretAccessKey
            )
        )
        amazonS3 = AmazonS3ClientBuilder.standard()
            .withCredentials(credentials)
            .withRegion(region)
            .build()
    }

    fun buildConditions(filePath: String): Conditions {
        val date = DateTimeFormatter.ofPattern("yyyyMMdd")!!.format(instant())
        val credential = String.format(xamzCredential, accessKeyId, date)
        val accessRule = CannedACL.valueOf(accessRuleCanned).toString()
        val expiration = DateTimeFormatter.ISO_DATE_TIME!!.format(instant().plus(Duration.parse(expireAfterDuration)))
        val amzDateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")!!.format(instant())

        return Conditions(date, bucketName, accessRule, uuid(), credential,
            amzDateTime, expiration, filePath, successActionRedirect, contentTypeStartsWith, metaTagStartsWith)
    }

    // TODO: we might need extract this class to separate file
    // and consider usage of Builder pattern instead of big constructor.
    class Conditions constructor(
        date: String,
        bucket: String,
        accessRule: String,
        uuid: String,
        credential: String,
        amzDateTime: String,
        expirationDate: String,
        uploadKey: String,
        successActionRedirect: String,
        contentTypeStartsWith: String,
        metaTagStartsWith: String
    ) {
        val date: String = date
        val bucket: Pair<String, String> = "bucket" to bucket
        val acl: Pair<String, String> = "acl" to accessRule
        val uuid: Pair<String, String> = "x-amz-meta-uuid" to uuid
        val serverSideEncryption: Pair<String, String> = "x-amz-server-side-encryption" to "AES256"
        val credential: Pair<String, String> = "x-amz-credential" to credential
        val algorithm: Pair<String, String> = "x-amz-algorithm" to "AWS4-HMAC-SHA256"
        val dateTime: Pair<String, String> = "x-amz-date" to amzDateTime
        val expiration: Pair<String, String> = "expiration" to expirationDate
        val uploadKey: Pair<String, String> = "key" to uploadKey
        val successActionRedirect: Pair<String, String> = "success_action_redirect" to successActionRedirect
        val contentTypeStartsWith: Pair<String, String> = "Content-Type" to contentTypeStartsWith
        val metaTagStartsWith: Pair<String, String> = "x-amz-meta-tag" to metaTagStartsWith

        override fun toString(): String {
            return "Conditions(date='$date'," +
                    " bucket=$bucket," +
                    " acl=$acl, uuid=$uuid," +
                    " serverSideEncryption=$serverSideEncryption," +
                    " credential=$credential, algorithm=$algorithm," +
                    " dateTime=$dateTime, expiration=$expiration," +
                    " uploadKey=$uploadKey, successActionRedirect=$successActionRedirect," +
                    " contentTypeStartsWith=$contentTypeStartsWith," +
                    " metaTagStartsWith=$metaTagStartsWith, uploadKey=$uploadKey)"
        }
    }
}
