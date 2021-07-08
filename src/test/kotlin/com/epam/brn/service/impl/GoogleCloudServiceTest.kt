package com.epam.brn.service.impl

import com.epam.brn.cloud.GoogleCloudService
import com.epam.brn.config.GoogleCloudConfig
import com.google.api.gax.paging.Page
import com.google.cloud.storage.Blob
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GoogleCloudServiceTest {

    @MockK
    lateinit var storage: Storage

    @MockK
    lateinit var cloudConfig: GoogleCloudConfig
    lateinit var googleCloudService: GoogleCloudService

    @BeforeEach
    fun setup() {
        every { cloudConfig.storage } returns storage
        every { cloudConfig.bucketName } returns ""
        googleCloudService = GoogleCloudService(cloudConfig)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should return correct folder structure`() {
        // GIVEN
        val resources = listOf(
            mockBlob("folder0/file1"),
            mockBlob("folder2/folder3/file4"),
            mockBlob("folder2/file5"),
            mockBlob("folder7/"),
            mockBlob("file6")
        )
        val pageBlob = mockk<Page<Blob>>()
        every { pageBlob.iterateAll() } returns resources
        val bucket = mockk<Bucket>()
        every { bucket.list() } returns pageBlob
        every { storage.get(any<String>()) } returns bucket

        // WHEN
        val bucketContent = googleCloudService.listBucket()
        val expected = listOf("folder0/", "folder2/", "folder2/folder3/", "folder7/")
        // THEN
        Assertions.assertEquals(expected, bucketContent)
    }

    private fun mockBlob(fileName: String): Blob {
        val blobOne = mockk<Blob>()
        every { blobOne.name } returns fileName
        return blobOne
    }
}
