package com.epam.brn.config

import com.amazonaws.services.glacier.model.CannedACL
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!integration-tests")
class AwsConfig {

    val dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")
    val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
    val expirationFormat = DateTimeFormatter.ISO_DATE_TIME

    @Value("\${cloud.expireAfterDuration}")
    val expireAfterDuration: String = ""
    @Value("\${aws.accessRuleCanned}")
    val accessRuleCanned: String = ""
    @Value("\${aws.accessKeyId}")
    val accessKeyId: String = ""
    @Value("\${aws.uploadKeyStartsWith}")
    val uploadKeyStartsWith: String = ""
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
    @Value("\${aws.secretAccessKey}")
    val secretAccessKey: String = ""
    @Value("\${aws.serviceName}")
    val serviceName: String = ""
    @Value("\${aws.region}")
    val region: String = ""

    @Value("\${aws.bucketLink}")
    val bucketLink: String = ""

    val accessRule: String by lazy { CannedACL.valueOf(accessRuleCanned).toString() }
    val expireAfter: Duration by lazy { Duration.parse(expireAfterDuration) }

    fun instant(): OffsetDateTime {
        return Instant.now().atOffset(ZoneOffset.UTC)
    }

    fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    inner class Conditions() {
        val now: OffsetDateTime = instant()
        val date: String = dateFormat(now)

        // POLICY CONDITIONS
        val bucket: Pair<String, String> = "bucket" to bucketName
        val acl: Pair<String, String> = "acl" to accessRule
        val uuid: Pair<String, String> = "x-amz-meta-uuid" to uuid()
        val serverSideEncryption: Pair<String, String> = "x-amz-server-side-encryption" to "AES256"
        val credential: Pair<String, String> = "x-amz-credential" to credentialFormat(now)
        val algorithm: Pair<String, String> = "x-amz-algorithm" to "AWS4-HMAC-SHA256"
        val dateTime: Pair<String, String> = "x-amz-date" to dateTimeFormat(now)
        val expiration: Pair<String, String> = "expiration" to expiration(now)
        val uploadKeyStartsWith: Pair<String, String> = "key" to this@AwsConfig.uploadKeyStartsWith
        val successActionRedirect: Pair<String, String> = "success_action_redirect" to this@AwsConfig.successActionRedirect
        val contentTypeStartsWith: Pair<String, String> = "Content-Type" to this@AwsConfig.contentTypeStartsWith
        val metaTagStartsWith: Pair<String, String> = "x-amz-meta-tag" to this@AwsConfig.metaTagStartsWith

        // UPLOAD FORM SPECIFIC DATA
        val uploadKey: Pair<String, String> = "key" to "${this@AwsConfig.uploadKeyStartsWith}\${filename}"

        private fun expiration(dateTime: OffsetDateTime): String =
            expirationFormat.format(dateTime.plus(expireAfter))
        private fun dateTimeFormat(dateTime: OffsetDateTime): String = dateTimeFormat.format(dateTime)
        private fun dateFormat(dateTime: OffsetDateTime): String = dateFormat.format(dateTime)
        private fun credentialFormat(dateTime: OffsetDateTime): String = String.format(xamzCredential, this.dateFormat(dateTime))
    }
}
