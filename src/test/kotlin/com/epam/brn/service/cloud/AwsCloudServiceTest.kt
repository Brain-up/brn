package com.epam.brn.service.cloud

import com.epam.brn.config.AwsConfig
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.assertFalse
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import software.amazon.awssdk.core.internal.waiters.DefaultWaiterResponse
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CommonPrefix
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.waiters.S3Waiter
import java.io.File
import java.util.Arrays

@ExtendWith(MockKExtension::class)
class AwsCloudServiceTest {

    companion object {
        const val X_AMZ_SIGNATURE = "x-amz-signature"
        const val X_AMZ_META_UUID = "x-amz-meta-uuid"
        const val X_AMZ_SERVER_SIDE_ENCRYPTION = "x-amz-server-side-encryption"
        const val X_AMZ_CREDENTIAL = "x-amz-credential"
        const val X_AMZ_ALGORITHM = "x-amz-algorithm"
        const val X_AMZ_DATE = "x-amz-date"
        const val ACL = "acl"
        const val BUCKET = "bucket"

        const val TEST_ACCESS_RULE = "private"
        const val TEST_BUCKET = "somebucket"
        const val TEST_UUID = "c49791b2-b27b-4edf-bac8-8734164c20e6"
        const val TEST_AMZ_DATE = "20200130T113917Z"
        const val TEST_ENC_ALGORYTHM = "AES256"
        const val TEST_HASH_ALGORYTHM = "AWS4-HMAC-SHA256"
        const val TEST_EXPIRATION_DATE = "2020-01-30T21:39:17.114Z"
        const val TEST_CREDENTIAL = "AKIAI7KLKATWVCMEKGPA/20200130/us-east-2/s3/aws4_request"
        const val TEST_FILEPATH = "tasks/\${filename}"
        const val TEST_DATE = "20200130"
        const val FOLDER = "folder/"
        const val FOLDER_2 = "folder2/"
        const val FOLDER_3 = "folder3/"
        const val FOLDER_SUBFOLDER = "$FOLDER/subfolder/"
        const val FOLDER_SUBFOLDER_2 = "$FOLDER/subfolder2/"
        const val FOLDER_SUBFOLDER_3 = "$FOLDER/subfolder3/"
        const val FOLDER_SUBFOLDER_4 = "$FOLDER/subfolder4/"
        const val FOLDER_SUBFOLDER_SUBDIR = "$FOLDER_SUBFOLDER/subdir/"
        const val FOLDER_SUBFOLDER_SUBDIR2 = "$FOLDER_SUBFOLDER/subdir2/"
        const val FOLDER_SUBFOLDER_3_SUBDIR = "$FOLDER_SUBFOLDER_3/subdir/"
        const val FOLDER_SUBFOLDER_3_SUBDIR2 = "$FOLDER_SUBFOLDER_3/subdir2/"

        const val UNVERIFIED_PATH = "unverified/"
    }

    @InjectMockKs
    lateinit var awsCloudService: AwsCloudService

    @MockK
    lateinit var awsConfig: AwsConfig

    @MockK
    lateinit var s3Client: S3Client

    @Test
    fun `should get correct signature for client upload`() {
        // GIVEN
        every { awsConfig.secretAccessKey } returns "99999999999999999999999999999"
        every { awsConfig.region } returns "us-east-2"
        every { awsConfig.serviceName } returns "s3"
        every { awsConfig.bucketLink } returns "https://somebucket.s3.amazonaws.com"

        val conditions: AwsConfig.Conditions = AwsConfig.Conditions(
            TEST_DATE,
            TEST_BUCKET, TEST_ACCESS_RULE,
            TEST_UUID,
            TEST_CREDENTIAL,
            TEST_AMZ_DATE,
            TEST_EXPIRATION_DATE,
            TEST_FILEPATH,
            "", "", ""
        )
        every { awsConfig.buildConditions(any()) } returns conditions

        // WHEN
        val actual = awsCloudService.uploadForm("")

        // THEN
        val expected: Map<String, Any> = mapOf(
            "action" to "https://somebucket.s3.amazonaws.com",
            "input" to listOf(
                mapOf("policy" to "ew0KICAiY29uZGl0aW9ucyIgOiBbIHsNCiAgICAiYnVja2V0IiA6ICJzb21lYnVja2V0Ig0KICB9LCB7DQogICAgImFjbCIgOiAicHJpdmF0ZSINCiAgfSwgWyAic3RhcnRzLXdpdGgiLCAiJGtleSIsICJ0YXNrcy8ke2ZpbGVuYW1lfSIgXSwgew0KICAgICJ4LWFtei1tZXRhLXV1aWQiIDogImM0OTc5MWIyLWIyN2ItNGVkZi1iYWM4LTg3MzQxNjRjMjBlNiINCiAgfSwgew0KICAgICJ4LWFtei1zZXJ2ZXItc2lkZS1lbmNyeXB0aW9uIiA6ICJBRVMyNTYiDQogIH0sIHsNCiAgICAieC1hbXotY3JlZGVudGlhbCIgOiAiQUtJQUk3S0xLQVRXVkNNRUtHUEEvMjAyMDAxMzAvdXMtZWFzdC0yL3MzL2F3czRfcmVxdWVzdCINCiAgfSwgew0KICAgICJ4LWFtei1hbGdvcml0aG0iIDogIkFXUzQtSE1BQy1TSEEyNTYiDQogIH0sIHsNCiAgICAieC1hbXotZGF0ZSIgOiAiMjAyMDAxMzBUMTEzOTE3WiINCiAgfSBdLA0KICAiZXhwaXJhdGlvbiIgOiAiMjAyMC0wMS0zMFQyMTozOToxNy4xMTRaIg0KfQ=="),
                mapOf(X_AMZ_SIGNATURE to "4d39e2b2ac5833352544d379dadad1ffba3148d9936d814f36f50b7af2cd8e8e"),
                mapOf("key" to TEST_FILEPATH),
                mapOf(ACL to TEST_ACCESS_RULE),
                mapOf(X_AMZ_META_UUID to TEST_UUID),
                mapOf(X_AMZ_SERVER_SIDE_ENCRYPTION to TEST_ENC_ALGORYTHM),
                mapOf(X_AMZ_CREDENTIAL to TEST_CREDENTIAL),
                mapOf(X_AMZ_ALGORITHM to TEST_HASH_ALGORYTHM),
                mapOf(X_AMZ_DATE to TEST_AMZ_DATE)
            )
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should convert to base64 string`() {
        // GIVEN
        val expected =
            "ew0KICAiY29uZGl0aW9ucyIgOiBbIHsNCiAgICAiYnVja2V0IiA6ICJzb21lYnVja2V0Ig0KICB9LCB7DQogICAgImFjbCIgOiAicHJpdmF0ZSINCiAgfSwgWyAic3RhcnRzLXdpdGgiLCAiJGtleSIsICJ0YXNrcy8ke2ZpbGVuYW1lfSIgXSwgew0KICAgICJ4LWFtei1tZXRhLXV1aWQiIDogImM0OTc5MWIyLWIyN2ItNGVkZi1iYWM4LTg3MzQxNjRjMjBlNiINCiAgfSwgew0KICAgICJ4LWFtei1zZXJ2ZXItc2lkZS1lbmNyeXB0aW9uIiA6ICJBRVMyNTYiDQogIH0sIHsNCiAgICAieC1hbXotY3JlZGVudGlhbCIgOiAiQUtJQUk3S0xLQVRXVkNNRUtHUEEvMjAyMDAxMzAvdXMtZWFzdC0yL3MzL2F3czRfcmVxdWVzdCINCiAgfSwgew0KICAgICJ4LWFtei1hbGdvcml0aG0iIDogIkFXUzQtSE1BQy1TSEEyNTYiDQogIH0sIHsNCiAgICAieC1hbXotZGF0ZSIgOiAiMjAyMDAxMzBUMTEzOTE3WiINCiAgfSBdLA0KICAiZXhwaXJhdGlvbiIgOiAiMjAyMC0wMS0zMFQyMTozOToxNy4xMTRaIg0KfQ=="
        val conditions = hashMapOf(
            "expiration" to TEST_EXPIRATION_DATE,
            "conditions" to listOf(
                hashMapOf(BUCKET to TEST_BUCKET),
                hashMapOf(ACL to TEST_ACCESS_RULE),
                arrayOf("starts-with", "\$key", TEST_FILEPATH),
                hashMapOf(X_AMZ_META_UUID to TEST_UUID),
                hashMapOf(X_AMZ_SERVER_SIDE_ENCRYPTION to TEST_ENC_ALGORYTHM),
                hashMapOf(X_AMZ_CREDENTIAL to TEST_CREDENTIAL),
                hashMapOf(X_AMZ_ALGORITHM to TEST_HASH_ALGORYTHM),
                hashMapOf(X_AMZ_DATE to TEST_AMZ_DATE)
            )
        )

        // WHEN
        val base64 = awsCloudService.toJsonBase64(conditions)

        // THEN
        assertEquals(expected, base64)
    }

    @Test
    fun `should get folder list`() {
        // GIVEN
        val returnFolders = mutableListOf<ListObjectsV2Response>()
        returnFolders.add(listObjectsV2Result(listOf(FOLDER, FOLDER_2, FOLDER_3)))
        returnFolders.add(
            listObjectsV2Result(
                listOf(
                    FOLDER_SUBFOLDER,
                    FOLDER_SUBFOLDER_2,
                    FOLDER_SUBFOLDER_3,
                    FOLDER_SUBFOLDER_4
                )
            )
        )
        returnFolders.add(listObjectsV2Result(listOf(FOLDER_SUBFOLDER_SUBDIR, FOLDER_SUBFOLDER_SUBDIR2)))
        returnFolders.add(listObjectsV2Result(listOf()))
        returnFolders.add(listObjectsV2Result(listOf()))
        returnFolders.add(listObjectsV2Result(listOf()))
        returnFolders.add(listObjectsV2Result(listOf(FOLDER_SUBFOLDER_3_SUBDIR, FOLDER_SUBFOLDER_3_SUBDIR2)))
        returnFolders.add(listObjectsV2Result(listOf()))
        returnFolders.add(listObjectsV2Result(listOf()))
        returnFolders.add(listObjectsV2Result(listOf()))
        returnFolders.add(listObjectsV2Result(listOf()))

        every { awsConfig.bucketName } returns TEST_BUCKET
        every { s3Client.listObjectsV2(any<ListObjectsV2Request>()) } returnsMany returnFolders

        // WHEN
        val listBucket = awsCloudService.getStorageFolders()

        // THEN
        val expected: List<String> = listOf(
            FOLDER, FOLDER_SUBFOLDER, FOLDER_SUBFOLDER_SUBDIR, FOLDER_SUBFOLDER_SUBDIR2,
            FOLDER_SUBFOLDER_2, FOLDER_SUBFOLDER_3, FOLDER_SUBFOLDER_3_SUBDIR, FOLDER_SUBFOLDER_3_SUBDIR2,
            FOLDER_SUBFOLDER_4, FOLDER_2, FOLDER_3
        )
        assertEquals(expected, listBucket)
    }

    @Test
    fun `should create folder`() {
        // GIVEN
        val folderPath = "new-folder/"
        val putObjectResponse = PutObjectResponse.builder()
            .eTag("tag")
            .build()
        val waiterResponse = DefaultWaiterResponse.builder<HeadObjectResponse>()
            .response(
                HeadObjectResponse.builder()
                    .eTag("tag")
                    .build()
            )
            .attemptsExecuted(1)
            .build()
        val putObjectRequestSlot = slot<PutObjectRequest>()
        val waitObjectRequestSlot = slot<HeadObjectRequest>()
        val requestBodySlot = slot<RequestBody>()
        val waiter = mockk<S3Waiter>()
        every { awsConfig.bucketName } returns BUCKET
        every { s3Client.putObject(capture(putObjectRequestSlot), capture(requestBodySlot)) } returns putObjectResponse
        every { s3Client.waiter() } returns waiter
        every { waiter.waitUntilObjectExists(capture(waitObjectRequestSlot)) } returns waiterResponse

        // WHEN
        awsCloudService.createFolder(folderPath)

        // THEN
        assert(requestBodySlot.captured.optionalContentLength().isPresent)
        assertEquals(0, requestBodySlot.captured.optionalContentLength().get())
        assertEquals(0, putObjectRequestSlot.captured.contentLength())
        assertEquals(folderPath, putObjectRequestSlot.captured.key())
        assertEquals(BUCKET, putObjectRequestSlot.captured.bucket())
        assertEquals(folderPath, waitObjectRequestSlot.captured.key())
        assertEquals(BUCKET, waitObjectRequestSlot.captured.bucket())
    }

    @Test
    fun `should create folder with correct delimiter for folder`() {
        // GIVEN
        val folderPath = "new-folder"
        val putObjectResponse = PutObjectResponse.builder()
            .eTag("tag")
            .build()
        val waiterResponse = DefaultWaiterResponse.builder<HeadObjectResponse>()
            .response(
                HeadObjectResponse.builder()
                    .eTag("tag")
                    .build()
            )
            .attemptsExecuted(1)
            .build()
        val putObjectRequestSlot = slot<PutObjectRequest>()
        val waitObjectRequestSlot = slot<HeadObjectRequest>()
        val requestBodySlot = slot<RequestBody>()
        val waiter = mockk<S3Waiter>()
        every { awsConfig.bucketName } returns BUCKET
        every { s3Client.putObject(capture(putObjectRequestSlot), capture(requestBodySlot)) } returns putObjectResponse
        every { s3Client.waiter() } returns waiter
        every { waiter.waitUntilObjectExists(capture(waitObjectRequestSlot)) } returns waiterResponse

        // WHEN
        awsCloudService.createFolder(folderPath)

        // THEN
        assert(requestBodySlot.captured.optionalContentLength().isPresent)
        assertEquals(0, requestBodySlot.captured.optionalContentLength().get())
        assertEquals(0, putObjectRequestSlot.captured.contentLength())
        assertEquals("$folderPath/", putObjectRequestSlot.captured.key())
        assertEquals(BUCKET, putObjectRequestSlot.captured.bucket())
        assertEquals("$folderPath/", waitObjectRequestSlot.captured.key())
        assertEquals(BUCKET, waitObjectRequestSlot.captured.bucket())
    }

    @Test
    fun `should return bucket url`() {
        // GIVEN
        val bucketUrl = "bucket-url"
        every { awsConfig.bucketLink } returns bucketUrl

        // WHEN
        val bucketUrlActual = awsCloudService.bucketUrl()

        // THEN
        assertEquals(bucketUrl, bucketUrlActual)
    }

    @Test
    fun `should return base file url`() {
        // GIVEN
        val baseFileUrl = "base-file-url"
        every { awsConfig.baseFileUrl } returns baseFileUrl

        // WHEN
        val baseFileUrlActual = awsCloudService.baseFileUrl()

        // THEN
        assertEquals(baseFileUrl, baseFileUrlActual)
    }

    @Test
    fun `should upload file to folder`() {
        // GIVEN
        val fileName = "file.name"
        val filePath = "some/path/"
        val file = File(this.javaClass.classLoader.getResource("inputData/test-file.txt")!!.file)
        val putObjectResponse = PutObjectResponse.builder()
            .eTag("tag")
            .build()
        val waiterResponse = DefaultWaiterResponse.builder<HeadObjectResponse>()
            .response(
                HeadObjectResponse.builder()
                    .eTag("tag")
                    .build()
            )
            .attemptsExecuted(1)
            .build()
        val putObjectRequestSlot = slot<PutObjectRequest>()
        val waitObjectRequestSlot = slot<HeadObjectRequest>()
        val requestBodySlot = slot<RequestBody>()
        val waiter = mockk<S3Waiter>()
        every { awsConfig.bucketName } returns BUCKET
        every { s3Client.putObject(capture(putObjectRequestSlot), capture(requestBodySlot)) } returns putObjectResponse
        every { s3Client.waiter() } returns waiter
        every { waiter.waitUntilObjectExists(capture(waitObjectRequestSlot)) } returns waiterResponse

        // WHEN
        awsCloudService.uploadFile(filePath, fileName, file.inputStream())

        // THEN
        assertEquals(BUCKET, putObjectRequestSlot.captured.bucket())
        assertEquals("$filePath$fileName", putObjectRequestSlot.captured.key())
        assertEquals(BUCKET, waitObjectRequestSlot.captured.bucket())
        assertEquals("$filePath$fileName", waitObjectRequestSlot.captured.key())
        val newStream = requestBodySlot.captured.contentStreamProvider().newStream()
        val expectedBody = IOUtils.toByteArray(file.inputStream())
        val actualBody = IOUtils.toByteArray(newStream)
        assert(Arrays.equals(expectedBody, actualBody))
    }

    @Test
    fun `should return fileNames for specified folder`() {
        // GIVEN
        val folderPath = "folder/path"
        val listObjectsV2Result = listObjectsV2Result(
            mutableListOf("folder/path/file1.png", "folder/path/file2.png")
        )
        every { awsConfig.bucketName } returns BUCKET
        every { s3Client.listObjectsV2(any<ListObjectsV2Request>()) } returns listObjectsV2Result

        // WHEN
        val actual = awsCloudService.getFileNames(folderPath)

        // THEN
        assertEquals(listOf("/file1.png", "/file2.png"), actual)
    }

    @Test
    fun `should return fileNames for main folder`() {
        // GIVEN
        ReflectionTestUtils.setField(awsCloudService, "defaultPicturesPath", "pictures/")
        val listObjectsV2Result = listObjectsV2Result(
            mutableListOf("folder/path/file1.png", "folder/path/file2.png")
        )
        every { awsConfig.bucketName } returns BUCKET
        every { s3Client.listObjectsV2(any<ListObjectsV2Request>()) } returns listObjectsV2Result

        // WHEN
        val actual = awsCloudService.getPicturesNamesFromMainFolder()

        // THEN
        assertEquals(listOf("/file1.png", "/file2.png"), actual)
    }

    @Test
    fun `should check is file exist`() {
        // GIVEN
        every { awsConfig.bucketName } returns BUCKET
        every { s3Client.headObject(any<HeadObjectRequest>()) } returns null

        // WHEN
        val actual = awsCloudService.isFileExist("testPath", "testName")

        // THEN
        assertTrue(actual)
    }

    @Test
    fun `should check file is not exist`() {
        // GIVEN
        every { awsConfig.bucketName } returns BUCKET
        every { s3Client.headObject(any<HeadObjectRequest>()) } throws (NoSuchKeyException.builder().build())

        // WHEN
        val actual = awsCloudService.isFileExist("testPath", "testName")

        // THEN
        assertFalse(actual)
    }

    @Test
    fun `should delete specified files`() {
        // GIVEN
        val filesToDelete = mutableListOf("folder/path/file1.png", "folder/path/file2.png")
        val expectedObjectIdentifiers = filesToDelete.map {
            ObjectIdentifier.builder().key(it).build()
        }
        val deleteRequestSlot = slot<DeleteObjectsRequest>()
        val deleteObjectsResponse = DeleteObjectsResponse.builder().build()
        every { awsConfig.bucketName } returns BUCKET
        every { s3Client.deleteObjects(capture(deleteRequestSlot)) } returns deleteObjectsResponse

        // WHEN
        awsCloudService.deleteFiles(filesToDelete)

        // THEN
        assertEquals(deleteRequestSlot.captured.delete().objects(), expectedObjectIdentifiers)
    }

    private fun listObjectsV2Result(keys: List<String>): ListObjectsV2Response {
        val result = mockk<ListObjectsV2Response>()
        val objectSummaries: List<CommonPrefix> = toObjectSummaries(keys)

        val contents = keys.map {
            S3Object.builder().key(it).build()
        }

        every { result.commonPrefixes() } returns objectSummaries
        every { result.contents() } returns contents
        return result
    }

    private fun toObjectSummaries(keys: List<String>): List<CommonPrefix> =
        keys.map { CommonPrefix.builder().prefix(it).build() }.toList()
}
