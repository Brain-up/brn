package com.epam.brn.cloud

import com.epam.brn.config.AwsConfig
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.utils.BinaryUtils
import java.io.File
import java.io.InputStream
import java.io.Serializable
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val FOLDER_DELIMETER = "/"

@ConditionalOnProperty(name = ["cloud.provider"], havingValue = "aws")
@Service
class AwsCloudService(@Autowired private val awsConfig: AwsConfig, @Autowired private val s3Client: S3Client) : CloudService {

    private val log = logger()

    private final val mapperIndented: ObjectWriter

    @Value("\${aws.resources.path.unverified:}")
    private val unverifiedPath: String = ""

    init {
        val indenter = DefaultIndenter().withLinefeed("\r\n")
        val printer = DefaultPrettyPrinter().withObjectIndenter(indenter)
        val objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        mapperIndented = objectMapper.writer(printer)
    }

    override fun bucketUrl(): String = awsConfig.bucketLink

    override fun baseFileUrl(): String = awsConfig.baseFileUrl

    override fun uploadForm(filePath: String): Map<String, Any> =
        signature(awsConfig.buildConditions(filePath))

    override fun uploadFile(filePath: String, fileName: String?, multipartFile: MultipartFile, isVerified: Boolean) {
        val fullFileName = createFullFileName(filePath, fileName ?: multipartFile.originalFilename, isVerified)
        uploadFile(fullFileName, multipartFile.inputStream)
    }

    override fun uploadFile(filePath: String, fileName: String?, file: File, isVerified: Boolean) {
        val fullFileName = createFullFileName(filePath, fileName ?: file.name, isVerified)
        uploadFile(fullFileName, file.inputStream())
    }

    override fun getListFolder(): List<String> {
        return getFolders("")
    }

    override fun createFolder(folderPath: String) {
        val fullFolderName = appendDelimiter(folderPath)
        val objectRequest = PutObjectRequest.builder()
            .bucket(awsConfig.bucketName)
            .key(fullFolderName)
            .contentLength(0)
            .build()

        s3Client.putObject(objectRequest, RequestBody.empty())

        waitRequestDone(fullFolderName)
        log.info("Folder $fullFolderName is ready")
    }

    override fun isFolderExists(folderPath: String): Boolean {
        val fullFolderName = appendDelimiter(folderPath)

        val request = ListObjectsV2Request.builder()
            .bucket(awsConfig.bucketName)
            .prefix(fullFolderName)
            .build()
        val result = s3Client.listObjectsV2(request)
        return result.hasContents()
    }

    private fun createFullFileName(
        path: String,
        filename: String?,
        isVerified: Boolean
    ): String {
        var fullFileName: String = if (!isVerified) {
            "$unverifiedPath$path"
        } else {
            path
        }

        if (StringUtils.endsWith(fullFileName, "/")) {
            fullFileName += filename
        }
        return fullFileName
    }

    private fun uploadFile(filePath: String, inputStream: InputStream) {
        val objectRequest = PutObjectRequest.builder()
            .bucket(awsConfig.bucketName)
            .key(filePath)
            .build()

        val byteArray = inputStream.readAllBytes()

        s3Client.putObject(objectRequest, RequestBody.fromBytes(byteArray))
        waitRequestDone(filePath)
        val fileName = filePath.substring(filePath.indexOfLast { it == '/' })
        log.info("File `$fileName` saved in S3 ${awsConfig.bucketName + filePath}")
    }

    private fun waitRequestDone(key: String) {
        val waiter = s3Client.waiter()
        val requestWait = HeadObjectRequest.builder()
            .bucket(awsConfig.bucketName)
            .key(key)
            .build()
        val waiterResponse = waiter.waitUntilObjectExists(requestWait)
        waiterResponse.matched().response().ifPresent(log::debug)
    }

    private fun appendDelimiter(folderPath: String): String {
        var key = folderPath
        if (!StringUtils.endsWith(key, FOLDER_DELIMETER)) {
            key += FOLDER_DELIMETER
        }
        return key
    }

    private fun getFolders(prefix: String): ArrayList<String> {
        val listObjectsV2Request = ListObjectsV2Request.builder()
            .delimiter(FOLDER_DELIMETER)
            .prefix(prefix)
            .bucket(awsConfig.bucketName)
            .build()
        val result = s3Client.listObjectsV2(listObjectsV2Request)
        val matchingKeys = result.commonPrefixes()
        val folders: ArrayList<String> = ArrayList()
        matchingKeys.forEach {
            val currentPrefix = it.prefix()
            folders.add(currentPrefix)
            folders.addAll(getFolders(prefix + currentPrefix))
        }
        return folders
    }

    private fun signature(conditions: AwsConfig.Conditions): Map<String, Serializable> {
        val policy: String = policy(conditions)
        val signature = sign(conditions.date, policy)

        val inputs = arrayListOf(mapOf("policy" to policy), mapOf("x-amz-signature" to signature))
        val conditions = listOf(
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
        )
        for (condition in conditions) {
            if (condition.second.isNotEmpty())
                inputs.add(mapOf(condition))
        }
        return mapOf("action" to awsConfig.bucketLink, "input" to inputs)
    }

    private fun policy(awsConditions: AwsConfig.Conditions): String {
        val includedFields: ArrayList<Any> = ArrayList()
        val policyConditions = listOf(
            awsConditions.bucket,
            awsConditions.acl,
            awsConditions.uploadKey,
            awsConditions.uuid,
            awsConditions.serverSideEncryption,
            awsConditions.credential,
            awsConditions.algorithm,
            awsConditions.dateTime,
            awsConditions.successActionRedirect,
            awsConditions.contentTypeStartsWith,
            awsConditions.metaTagStartsWith,
        )
        for (condition in policyConditions) {
            if (condition.second.isNotEmpty()) {
                with(awsConditions) {
                    if (condition in arrayOf(uploadKey, contentTypeStartsWith, metaTagStartsWith))
                        includedFields.add(arrayOf("starts-with", "\$${condition.first}", condition.second))
                    else
                        includedFields.add(hashMapOf(condition))
                }
            }
        }
        val policy = hashMapOf(
            awsConditions.expiration,
            "conditions" to includedFields
        )
        return toJsonBase64(policy)
    }

    fun toJsonBase64(rawObject: Any): String = BinaryUtils.toBase64(mapperIndented.writeValueAsBytes(rawObject))

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
