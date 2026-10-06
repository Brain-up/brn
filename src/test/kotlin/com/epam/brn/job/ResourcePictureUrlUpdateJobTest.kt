package com.epam.brn.job

import com.epam.brn.model.Resource
import com.epam.brn.service.ResourceService
import com.epam.brn.service.cloud.CloudService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockKExtension::class)
internal class ResourcePictureUrlUpdateJobTest {
    @InjectMockKs
    private lateinit var resourcePictureUrlUpdateJob: ResourcePictureUrlUpdateJob

    @MockK
    private lateinit var cloudService: CloudService

    @MockK
    private lateinit var resourceService: ResourceService

    private val defaultPicturesPath = "pictures/"
    private val unverifiedPicturesPath = "pictures-unverified/"

    @BeforeEach
    fun init() {
        ReflectionTestUtils.setField(resourcePictureUrlUpdateJob, "defaultPicturesPath", defaultPicturesPath)
        ReflectionTestUtils.setField(resourcePictureUrlUpdateJob, "unverifiedPicturesPath", unverifiedPicturesPath)
    }

    @Test
    fun `should update url from default folder`() {
        // GIVEN
        val word = "дом"
        val defaultFolderPictures = mapOf(word to "$defaultPicturesPath$word.png")
        val unverifiedFolderPictures = mapOf(word to "$unverifiedPicturesPath$word.png")
        every { cloudService.getFilePathMap(defaultPicturesPath) } returns defaultFolderPictures
        every { cloudService.getFilePathMap(unverifiedPicturesPath) } returns unverifiedFolderPictures

        val resource = Resource(word = word)
        val resources = listOf(resource)
        every { resourceService.findAll() } returns resources
        every { resourceService.saveAll(resources) } returns resources

        // WHEN
        val jobResponse = resourcePictureUrlUpdateJob.updatePictureUrl()

        // THEN
        resource.pictureFileUrl shouldBe defaultFolderPictures[resource.word]
        jobResponse.inDefaultFolderPicturesCount shouldBe 1
        jobResponse.inUnverifiedFolderPicturesCount shouldBe 1
        jobResponse.withCorrectDefaultUrlResources shouldBe 1
        jobResponse.withUnverifiedUrlResources shouldBe 0
        jobResponse.success shouldBe true
    }

    @Test
    fun `should update url from unverified folder`() {
        // GIVEN
        val word = "дом"
        val defaultFolderPictures = mapOf("бабушка" to "${defaultPicturesPath}бабушка.png")
        val unverifiedFolderPictures = mapOf(word to "$unverifiedPicturesPath$word.png")
        every { cloudService.getFilePathMap(defaultPicturesPath) } returns defaultFolderPictures
        every { cloudService.getFilePathMap(unverifiedPicturesPath) } returns unverifiedFolderPictures

        val resource = Resource(word = word)
        val resources = listOf(resource)
        every { resourceService.findAll() } returns resources
        every { resourceService.saveAll(resources) } returns resources

        // WHEN
        val jobResponse = resourcePictureUrlUpdateJob.updatePictureUrl()

        // THEN
        resource.pictureFileUrl shouldBe unverifiedFolderPictures[resource.word]
        jobResponse.inDefaultFolderPicturesCount shouldBe 1
        jobResponse.inUnverifiedFolderPicturesCount shouldBe 1
        jobResponse.withCorrectDefaultUrlResources shouldBe 0
        jobResponse.withUnverifiedUrlResources shouldBe 1
        jobResponse.success shouldBe true
    }

    @Test
    fun `should not update url if it was not changed`() {
        // GIVEN
        val word = "дом"
        val defaultFolderPictures = mapOf(word to "$defaultPicturesPath$word.png")
        val unverifiedFolderPictures = mapOf(word to "$unverifiedPicturesPath$word.png")
        every { cloudService.getFilePathMap(defaultPicturesPath) } returns defaultFolderPictures
        every { cloudService.getFilePathMap(unverifiedPicturesPath) } returns unverifiedFolderPictures

        val resource = Resource(word = word, pictureFileUrl = "$defaultPicturesPath$word.png")
        val resources = listOf(resource)
        every { resourceService.findAll() } returns resources

        // WHEN
        resourcePictureUrlUpdateJob.updatePictureUrl()

        // THEN
        verify(exactly = 0) { resourceService.saveAll(any()) }
    }

    @Test
    fun `should clean url if picture is removed`() {
        // GIVEN
        val word = "дом"
        val defaultFolderPictures = mapOf("бабушка" to "${defaultPicturesPath}бабушка.png")
        val unverifiedFolderPictures = mapOf("бабушка" to "${unverifiedPicturesPath}бабушка.png")
        every { cloudService.getFilePathMap(defaultPicturesPath) } returns defaultFolderPictures
        every { cloudService.getFilePathMap(unverifiedPicturesPath) } returns unverifiedFolderPictures

        val resource = Resource(word = word, pictureFileUrl = "pictures/дом.png")
        val resources = listOf(resource)
        every { resourceService.findAll() } returns resources
        every { resourceService.saveAll(resources) } returns resources

        // WHEN
        val jobResponse = resourcePictureUrlUpdateJob.updatePictureUrl()

        // THEN
        resource.pictureFileUrl!!.isEmpty() shouldBe true
        jobResponse.inDefaultFolderPicturesCount shouldBe 1
        jobResponse.inUnverifiedFolderPicturesCount shouldBe 1
        jobResponse.withUnverifiedUrlResources shouldBe 0
        jobResponse.withCorrectDefaultUrlResources shouldBe 0
        jobResponse.resourcesWithoutPictures shouldBe 1
        jobResponse.success shouldBe true
    }

    @Test
    fun `should be unsuccessful in case of errors`() {
        // GIVEN
        val errorMessage = "Error with S3"
        every { cloudService.getFilePathMap(defaultPicturesPath) } throws RuntimeException(errorMessage)

        // WHEN
        val jobResponse = resourcePictureUrlUpdateJob.updatePictureUrl()

        // THEN
        jobResponse.success shouldBe false
        jobResponse.errorMessage shouldBe errorMessage
    }
}
