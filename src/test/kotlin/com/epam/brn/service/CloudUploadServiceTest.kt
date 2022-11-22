package com.epam.brn.service

import com.epam.brn.model.Resource
import com.epam.brn.service.cloud.CloudService
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.MultipartFile

@ExtendWith(MockKExtension::class)
@DisplayName("CloudUploadServiceTest test using MockK")
internal class CloudUploadServiceTest {

    @InjectMockKs
    private lateinit var cloudUploadService: CloudUploadService

    @MockK
    lateinit var cloudService: CloudService

    @MockK
    lateinit var resourceService: ResourceService

    val maxSize = 100L
    val allowedExtensions = setOf("png", "jpg")
    val defaultPicturesPath = "default/"
    val unverifiedPicturesPath = "path/"
    val contributorPicturesPath = "contributors/"

    @BeforeEach
    fun setup() {
        cloudUploadService.unverifiedPictureExtensions = allowedExtensions
        cloudUploadService.contributorPictureExtensions = allowedExtensions
        cloudUploadService.unverifiedPictureMaxSize = DataSize.ofBytes(maxSize)
        cloudUploadService.contributorPictureMaxSize = DataSize.ofBytes(maxSize)
        cloudUploadService.defaultPicturesPath = defaultPicturesPath
        cloudUploadService.unverifiedPicturesPath = unverifiedPicturesPath
        cloudUploadService.contributorPicturesPath = contributorPicturesPath
    }

    @Test
    fun `should throw IllegalArgumentException when unverified picture file extension is not supported`() {
        // GIVEN
        val mockMultipartFile = mockk<MultipartFile>()
        every { mockMultipartFile.size } returns maxSize
        every { mockMultipartFile.originalFilename } returns "filename.not-allowed-ext"

        // WHEN
        val exception = shouldThrowExactly<IllegalArgumentException> {
            cloudUploadService.uploadUnverifiedPictureFile(mockMultipartFile)
        }

        // THEN
        exception.message shouldBe "File extension should be one of $allowedExtensions"
        verify(exactly = 0) { cloudService.isFileExist(any(), any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when contributor picture file extension is not supported`() {
        // GIVEN
        val mockMultipartFile = mockk<MultipartFile>()
        val fileName = "fileName"
        every { mockMultipartFile.size } returns maxSize
        every { mockMultipartFile.originalFilename } returns "filename.not-allowed-ext"

        // WHEN
        val exception = shouldThrowExactly<IllegalArgumentException> {
            cloudUploadService.uploadContributorPicture(mockMultipartFile, fileName)
        }

        // THEN
        exception.message shouldBe "File extension should be one of $allowedExtensions"
        verify(exactly = 0) { cloudService.isFileExist(any(), any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when unverified picture file size is too big`() {
        // GIVEN
        val fileSize = 150L
        val mockMultipartFile = mockk<MultipartFile>()
        every { mockMultipartFile.size } returns fileSize
        every { mockMultipartFile.originalFilename } returns "fileName.png"

        // WHEN
        val exception = shouldThrowExactly<IllegalArgumentException> {
            cloudUploadService.uploadUnverifiedPictureFile(mockMultipartFile)
        }

        // THEN
        exception.message shouldBe "File size [$fileSize] should be less than max file size " +
            "[${FileUtils.byteCountToDisplaySize(maxSize)}]"
        verify(exactly = 0) { cloudService.isFileExist(any(), any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when contributor picture file size is too big`() {
        // GIVEN
        val fileSize = 150L
        val mockMultipartFile = mockk<MultipartFile>()
        val fileName = "fileName"
        every { mockMultipartFile.size } returns fileSize
        every { mockMultipartFile.originalFilename } returns "fileName.png"

        // WHEN
        val exception = shouldThrowExactly<IllegalArgumentException> {
            cloudUploadService.uploadContributorPicture(mockMultipartFile, fileName)
        }

        // THEN
        exception.message shouldBe "File size [$fileSize] should be less than max file size " +
            "[${FileUtils.byteCountToDisplaySize(maxSize)}]"
        verify(exactly = 0) { cloudService.isFileExist(any(), any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when unverified picture file name not exist in database as word`() {
        // GIVEN
        val word = "Word"
        val fullFileName = "$word.png"

        val mockMultipartFile = mockk<MultipartFile>()
        every { mockMultipartFile.size } returns maxSize
        every { mockMultipartFile.originalFilename } returns fullFileName
        every { resourceService.findFirstResourceByWord(word) } returns null

        // WHEN
        val exception = shouldThrowExactly<IllegalArgumentException> {
            cloudUploadService.uploadUnverifiedPictureFile(mockMultipartFile)
        }

        // THEN
        exception.message shouldBe "The world \"$word\" is not found in database"
        verify(exactly = 0) { cloudService.isFileExist(any(), any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when unverified picture file is exist in cloud default picture folder`() {
        // GIVEN
        val word = "Word"
        val fullFileName = "$word.png"

        val mockMultipartFile = mockk<MultipartFile>()
        every { mockMultipartFile.size } returns maxSize
        every { mockMultipartFile.originalFilename } returns fullFileName
        every { resourceService.findFirstResourceByWord(word) } returns Resource(id = 1)
        every { cloudService.isFileExist(defaultPicturesPath, word) } returns true

        // WHEN
        val exception = shouldThrowExactly<IllegalArgumentException> {
            cloudUploadService.uploadUnverifiedPictureFile(mockMultipartFile)
        }

        // THEN
        exception.message shouldBe "File \"$fullFileName\" is exist in cloud default path"
        verify(exactly = 1) { cloudService.isFileExist(defaultPicturesPath, word) }
        verify(exactly = 0) { cloudService.isFileExist(unverifiedPicturesPath, word) }
    }

    @Test
    fun `should throw IllegalArgumentException when file is exist in cloud unverified picture folder`() {
        // GIVEN
        val word = "Word"
        val fullFileName = "$word.png"

        val mockMultipartFile = mockk<MultipartFile>()
        every { mockMultipartFile.size } returns maxSize
        every { mockMultipartFile.originalFilename } returns fullFileName
        every { resourceService.findFirstResourceByWord(word) } returns Resource(id = 1)
        every { cloudService.isFileExist(defaultPicturesPath, word) } returns false
        every { cloudService.isFileExist(unverifiedPicturesPath, word) } returns true

        // WHEN
        val exception = shouldThrowExactly<IllegalArgumentException> {
            cloudUploadService.uploadUnverifiedPictureFile(mockMultipartFile)
        }

        // THEN
        exception.message shouldBe "File \"$fullFileName\" is exist in cloud path \"$unverifiedPicturesPath\""
        verify(exactly = 1) { cloudService.isFileExist(defaultPicturesPath, word) }
        verify(exactly = 1) { cloudService.isFileExist(unverifiedPicturesPath, word) }
    }

    @Test
    fun `should call uploadFile on cloudService for unverified pictures when all OK`() {
        // GIVEN
        val word = "Word"
        val fullFileName = "$word.png"

        val fileContent = "some test data for my input stream"
        val inputStream = IOUtils.toInputStream(fileContent, Charsets.UTF_8)
        val mockMultipartFile = mockk<MultipartFile>()
        every { mockMultipartFile.size } returns maxSize
        every { mockMultipartFile.originalFilename } returns fullFileName
        every { mockMultipartFile.inputStream } returns inputStream
        every { resourceService.findFirstResourceByWord(word) } returns Resource(id = 1)
        every { cloudService.isFileExist(defaultPicturesPath, word) } returns false
        every { cloudService.isFileExist(unverifiedPicturesPath, word) } returns false
        every { cloudService.uploadFile(unverifiedPicturesPath, fullFileName, inputStream) } returns Unit

        // WHEN
        cloudUploadService.uploadUnverifiedPictureFile(mockMultipartFile)

        // THEN
        verify(exactly = 2) { cloudService.isFileExist(any(), any()) }
        verify(exactly = 1) { cloudService.uploadFile(unverifiedPicturesPath, fullFileName, inputStream) }
    }

    @Test
    fun `should call uploadFile on cloudService for contributor pictures when all OK`() {
        // GIVEN
        val fileName = "Word"
        val fullFileName = "$fileName.png"

        val fileContent = "some test data for my input stream"
        val inputStream = IOUtils.toInputStream(fileContent, Charsets.UTF_8)
        val mockMultipartFile = mockk<MultipartFile>()
        every { mockMultipartFile.size } returns maxSize
        every { mockMultipartFile.originalFilename } returns fullFileName
        every { mockMultipartFile.inputStream } returns inputStream
        every { cloudService.baseFileUrl() } returns "baseFileUrl"
        every { cloudService.uploadFile(contributorPicturesPath, fullFileName, inputStream) } returns Unit

        // WHEN
        val filePath = cloudUploadService.uploadContributorPicture(mockMultipartFile, fileName)

        // THEN
        verify(exactly = 1) { cloudService.uploadFile(contributorPicturesPath, fullFileName, inputStream) }
        filePath shouldBe "baseFileUrl/$contributorPicturesPath/$fullFileName"
    }
}
