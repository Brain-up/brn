package com.epam.brn.cloud

import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.util.Base64
import com.epam.brn.config.AwsConfig
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.io.Serializable
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@ConditionalOnProperty(name = ["cloud.provider"], havingValue = "aws")
@Service
class AwsCloudService(@Autowired private val awsConfig: AwsConfig) : CloudService {
    private final val mapperIndented: ObjectWriter
    init {
        val indenter = DefaultIndenter().withLinefeed("\r\n")
        val printer = DefaultPrettyPrinter().withObjectIndenter(indenter)
        val objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        mapperIndented = objectMapper.writer(printer)
    }

    override fun bucketUrl(): String = awsConfig.bucketLink

    override fun uploadForm(filePath: String): Map<String, Any> =
        signature(awsConfig.buildConditions(filePath))

    override fun listBucket(): List<String> {
        val amazonS3 = awsConfig.amazonS3
        val folders: ArrayList<String> = ArrayList()

        var continuationToken: String? = null
        while (true) {
            val listObjectsV2Request = ListObjectsV2Request()
            listObjectsV2Request.bucketName = awsConfig.bucketName
            listObjectsV2Request.continuationToken = continuationToken
            val result = amazonS3.listObjectsV2(listObjectsV2Request)
            val matchingKeys = result.objectSummaries.stream().map { it.key }.filter { it.endsWith("/") }
            matchingKeys.forEach { folders.add(it) }
            if (!result.isTruncated)
                break
            continuationToken = result.nextContinuationToken
        }

        return folders
    }

    private fun signature(conditions: AwsConfig.Conditions): Map<String, Serializable> {
        val policy: String = policy(conditions)
        val signature = sign(conditions.date, policy)

        val inputs = arrayListOf(mapOf("policy" to policy), mapOf("x-amz-signature" to signature))
        for (condition in listOf(
            conditions.uploadKey,
            conditions.acl,
            conditions.uuid,
            conditions.serverSideEncryption,
            conditions.credential,
            conditions.algorithm,
            conditions.dateTime,
            conditions.successActionRedirect,
            conditions.contentTypeStartsWith,
            conditions.metaTagStartsWith
        )) {
            if (condition.second.isNotEmpty())
                inputs.add(mapOf(condition))
        }
        return mapOf("action" to awsConfig.bucketLink, "input" to inputs)
    }

    private fun policy(conditions: AwsConfig.Conditions): String {
        val includedFields: ArrayList<Any> = ArrayList()
        for (condition in listOf(
            conditions.bucket,
            conditions.acl,
            conditions.uploadKey,
            conditions.uuid,
            conditions.serverSideEncryption,
            conditions.credential,
            conditions.algorithm,
            conditions.dateTime,
            conditions.successActionRedirect,
            conditions.contentTypeStartsWith,
            conditions.metaTagStartsWith
        )) {
            if (condition.second.isNotEmpty()) {
                if (condition in arrayOf(conditions.uploadKey, conditions.contentTypeStartsWith, conditions.metaTagStartsWith))
                    includedFields.add(arrayOf("starts-with", "\$${condition.first}", condition.second))
                else
                    includedFields.add(hashMapOf(condition))
            }
        }
        val policy = hashMapOf(
            conditions.expiration,
            "conditions" to includedFields
        )
        return toJsonBase64(policy)
    }

    fun toJsonBase64(rawObject: Any): String = Base64.encodeAsString(*mapperIndented.writeValueAsBytes(rawObject))

    private fun sign(date: String, policy: String): String {
        val signature = getSignatureKey(awsConfig.secretAccessKey, date, awsConfig.region, awsConfig.serviceName)
        val hmacSHA256 = hmacSHA256(policy, signature)
        return toHex(hmacSHA256)
    }

    private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
        val kSecret = ("AWS4$key").toByteArray()
        val kDate = hmacSHA256(dateStamp, kSecret)
        val kRegion = hmacSHA256(regionName, kDate)
        val kService = hmacSHA256(serviceName, kRegion)
        return hmacSHA256("aws4_request", kService)
    }

    private fun hmacSHA256(data: String, key: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray())
    }

    private fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }
}
