package com.epam.brn.controller

import com.epam.brn.service.CloudUploadService
import com.epam.brn.service.cloud.CloudService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile

@ExtendWith(MockKExtension::class)
@DisplayName("CloudControllerTest test using MockK")
internal class CloudControllerTest {
    @InjectMockKs
    private lateinit var cloudController: CloudController

    @MockK
    lateinit var cloudService: CloudService

    @MockK
    lateinit var cloudUploadService: CloudUploadService

    @Test
    fun `should upload signature for client direct`() {
        // GIVEN
        val filePath = "link"
        val baseSingleObjectResponseDto = mapOf("1" to 1)
        every { cloudService.uploadForm(filePath) } returns baseSingleObjectResponseDto

        // WHEN
        val signatureForClientDirectUpload = cloudController.signatureForClientDirectUpload(filePath)

        // THEN
        signatureForClientDirectUpload.statusCode.value() shouldBe HttpStatus.SC_OK
        signatureForClientDirectUpload.body!!.data shouldBe baseSingleObjectResponseDto
        verify(exactly = 1) { cloudService.uploadForm(filePath) }
    }

    @Test
    fun `should get bucket url`() {
        // GIVEN
        val urlContent = "url"
        every { cloudService.bucketUrl() } returns urlContent

        // WHEN
        val actualBucketUrl = cloudController.bucketUrl()

        // THEN
        verify(exactly = 1) { cloudService.bucketUrl() }
        actualBucketUrl.statusCodeValue shouldBe HttpStatus.SC_OK
        actualBucketUrl.body!!.data shouldBe urlContent
    }

    @Test
    fun `should get base file Url`() {
        // GIVEN
        val baseFile = "url"
        every { cloudService.baseFileUrl() } returns baseFile

        // WHEN
        val actualBaseFileUrl = cloudController.baseFileUrl()

        // THEN
        verify(exactly = 1) { cloudService.baseFileUrl() }
        actualBaseFileUrl.statusCodeValue shouldBe HttpStatus.SC_OK
        actualBaseFileUrl.body!!.data shouldBe baseFile
    }

    @Test
    fun `should get folders in bucket`() {
        // GIVEN
        val listBucket = listOf("folderName")
        every { cloudService.getStorageFolders() } returns listBucket

        // WHEN
        val actualListBucket = cloudController.listBucket()

        // THEN
        verify(exactly = 1) { cloudService.getStorageFolders() }
        actualListBucket.statusCodeValue shouldBe HttpStatus.SC_OK
        actualListBucket.body!!.data shouldBe listBucket
    }

    @Test
    fun `loadUnverifiedPicture should call cloud service upload file and return status OK`() {
        // GIVEN
        val data = "SOMEDATA".toByteArray().inputStream()
        val multipartFile = mockk<MultipartFile>()
        val fileName = "filename.png"
        every { multipartFile.originalFilename } returns fileName
        every { multipartFile.inputStream } returns data
        every { cloudUploadService.uploadUnverifiedPictureFile(multipartFile) } returns Unit

        // WHEN
        val response = cloudController.loadUnverifiedPicture(multipartFile)

        // THEN
        response.statusCode.value() shouldBe HttpStatus.SC_CREATED
        verify(exactly = 1) { cloudUploadService.uploadUnverifiedPictureFile(multipartFile) }
    }

    @Test
    fun `uploadContributorPicture should call cloud service upload file and return status OK`() {
        // GIVEN
        val data = "SOMEDATA".toByteArray().inputStream()
        val multipartFile = mockk<MultipartFile>()
        val fileNameWithExtension = "filename.png"
        val fileName = "file"
        every { multipartFile.originalFilename } returns fileNameWithExtension
        every { multipartFile.inputStream } returns data
        every { cloudUploadService.uploadContributorPicture(multipartFile, fileName) } returns "contributor/$fileNameWithExtension"

        // WHEN
        val response = cloudController.uploadContributorPicture(multipartFile, fileName)

        // THEN
        response.statusCode.value() shouldBe HttpStatus.SC_CREATED
        verify(exactly = 1) { cloudUploadService.uploadContributorPicture(multipartFile, any()) }
    }
}
