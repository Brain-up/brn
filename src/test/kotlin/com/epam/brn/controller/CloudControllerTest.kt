package com.epam.brn.controller

import com.epam.brn.cloud.CloudService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@DisplayName("CloudControllerTest test using mockito")
internal class CloudControllerTest {

    @InjectMockKs
    private lateinit var cloudController: CloudController

    @MockK
    lateinit var cloudService: CloudService

    @Test
    fun `should upload signature for client direct`() {

        // GIVEN
        val filePath = "link"
        val baseSingleObjectResponseDto = mapOf("1" to 1)
        every { cloudService.uploadForm(filePath) } returns baseSingleObjectResponseDto

        // WHEN
        val signatureForClientDirectUpload = cloudController.signatureForClientDirectUpload(filePath)

        // THEN
        assertEquals(HttpStatus.SC_OK, signatureForClientDirectUpload.statusCode.value())
        assertEquals(baseSingleObjectResponseDto, signatureForClientDirectUpload.body!!.data)
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
        assertEquals(HttpStatus.SC_OK, actualBucketUrl.statusCodeValue)
        assertEquals(urlContent, actualBucketUrl.body!!.data)
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
        assertEquals(HttpStatus.SC_OK, actualBaseFileUrl.statusCodeValue)
        assertEquals(baseFile, actualBaseFileUrl.body!!.data)
    }

    @Test
    fun `should get folders in bucket`() {

        // GIVEN
        val listBucket = listOf("folderName")
        every { cloudService.listBucket() } returns listBucket

        // WHEN
        val actualListBucket = cloudController.listBucket()

        // THEN
        verify(exactly = 1) { cloudService.listBucket() }
        assertEquals(HttpStatus.SC_OK, actualListBucket.statusCodeValue)
        assertEquals(listBucket, actualListBucket.body!!.data)
    }
}
