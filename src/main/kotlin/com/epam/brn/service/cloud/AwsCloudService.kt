package com.epam.brn.service.cloud

import com.epam.brn.config.AwsConfig
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.utils.BinaryUtils
import java.io.InputStream
import java.io.Serializable
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@ConditionalOnProperty(name = ["cloud.provider"], havingValue = "aws")
@Service
class AwsCloudService(@Autowired private val awsConfig: AwsConfig, @Autowired private val s3Client: S3Client) :
    CloudService {

    companion object {
        private const val FOLDER_DELIMITER = "/"
    }

    private val log = logger()

    private final val mapperIndented: ObjectWriter

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

    override fun uploadFile(path: String, fileName: String, inputStream: InputStream) {
        val fullFileName = createFullFileName(path, fileName)
        uploadFile(fullFileName, inputStream)
    }

    override fun getStorageFolders(): List<String> {
        return getFolders("")
    }

    override fun getFileNames(folderPath: String): List<String> {
        val request = ListObjectsV2Request.builder()
            .bucket(awsConfig.bucketName)
            .prefix(folderPath)
            .build()
        return s3Client.listObjectsV2(request).contents().map {
            it.key().substring(it.key().lastIndexOf(FOLDER_DELIMITER))
        }
    }

    override fun deleteFiles(fileNames: List<String>) {
        val objectIdentifiersToDelete = fileNames.map {
            ObjectIdentifier.builder().key(it).build()
        }

        val request = DeleteObjectsRequest.builder()
            .bucket(awsConfig.bucketName)
            .delete(Delete.builder().objects(objectIdentifiersToDelete).build())
            .build()
        val response = s3Client.deleteObjects(request)

        if (response.errors().size > 0)
            log.warn("Deletion of $fileNames failed.")
        else
            log.info("Files $fileNames are deleted")
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

    override fun isFileExist(filePath: String, fileName: String): Boolean {
        val fullFileName = createFullFileName(filePath, fileName)

        val request = ListObjectsV2Request.builder()
            .bucket(awsConfig.bucketName)
            .prefix(fullFileName)
            .build()
        val result = s3Client.listObjectsV2(request)
        return result.hasContents()
    }

    private fun createFullFileName(
        path: String,
        filename: String
    ): String {
        var fullFileName = path
        if (!StringUtils.endsWith(fullFileName, FOLDER_DELIMITER)) {
            fullFileName += FOLDER_DELIMITER
        }
        fullFileName += filename
        return fullFileName
    }

    private fun uploadFile(filePath: String, inputStream: InputStream) {
        val objectRequest = PutObjectRequest.builder()
            .bucket(awsConfig.bucketName)
            .key(filePath)
            .build()

        val byteArray = IOUtils.toByteArray(inputStream)

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
        if (!StringUtils.endsWith(key, FOLDER_DELIMITER)) {
            key += FOLDER_DELIMITER
        }
        return key
    }

    private fun getFolders(prefix: String): ArrayList<String> {
        val listObjectsV2Request = ListObjectsV2Request.builder()
            .delimiter(FOLDER_DELIMITER)
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
