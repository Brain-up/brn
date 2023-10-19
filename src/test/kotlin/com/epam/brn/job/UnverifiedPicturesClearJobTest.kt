package com.epam.brn.job

import com.epam.brn.service.cloud.CloudService
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.slot
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class UnverifiedPicturesClearJobTest {
    @InjectMockKs
    lateinit var unverifiedPicturesClearJob: UnverifiedPicturesClearJob

    @MockK
    lateinit var cloudService: CloudService

    @Test
    fun `should choose right files for deletion`() {
        // GIVEN
        ReflectionTestUtils.setField(unverifiedPicturesClearJob, "defaultPicturesPath", "defaultPicturesPath")
        ReflectionTestUtils.setField(unverifiedPicturesClearJob, "unverifiedPicturesPath", "unverifiedPicturesPath")
        val capturedFileNames = slot<List<String>>()

        every { cloudService.getFileNames("defaultPicturesPath") } returns listOf("/file1.png", "/file2.png", "/")
        every { cloudService.getFileNames("unverifiedPicturesPath") } returns listOf("/file2.png", "/file3.png", "/")
        every { cloudService.deleteFiles(capture(capturedFileNames)) } just Runs

        // WHEN
        unverifiedPicturesClearJob.clearUnusedPictures()

        // THEN
        assertEquals(capturedFileNames.captured, listOf("unverifiedPicturesPath/file2.png"))
    }
}
