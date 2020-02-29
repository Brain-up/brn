package com.epam.brn.service.impl

import com.epam.brn.config.GoogleCloudConfig
import com.google.api.gax.paging.Page
import com.google.cloud.storage.Blob
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class GoogleCloudServiceTest {

    @Mock
    lateinit var storage: Storage
    @Mock
    lateinit var cloudConfig: GoogleCloudConfig
    lateinit var googleCloudService: GoogleCloudService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(cloudConfig.storage).thenReturn(storage)
        Mockito.`when`(cloudConfig.bucketName).thenReturn("")
        googleCloudService = GoogleCloudService(cloudConfig)
    }

    @Test
    fun `should return correct folder structure`() {
        // GIVEN
        val resources: ArrayList<Blob> = ArrayList()
        resources.addAll(
            arrayOf(
                mockBlob("folder0/file1"),
                mockBlob("folder2/folder3/file4"),
                mockBlob("folder2/file5"),
                mockBlob("folder7/"),
                mockBlob("file6")
            )
        )
        var pageBlob = Mockito.mock(Page::class.java) as Page<Blob>
        Mockito.`when`(pageBlob.iterateAll()).thenReturn(resources)
        var bucket = Mockito.mock(Bucket::class.java)
        Mockito.`when`(bucket.list()).thenReturn(pageBlob)
        Mockito.`when`(storage.get(anyString())).thenReturn(bucket)
        // WHEN
        val bucketContent = googleCloudService.listBucket()
        val expected = listOf("folder0/", "folder2/", "folder2/folder3/", "folder7/")
        // THEN
        Assertions.assertEquals(expected, bucketContent)
    }

    private fun mockBlob(fileName: String): Blob {
        val blobOne: Blob = Mockito.mock(Blob::class.java)
        Mockito.`when`(blobOne.name).thenReturn(fileName)
        return blobOne
    }
}
