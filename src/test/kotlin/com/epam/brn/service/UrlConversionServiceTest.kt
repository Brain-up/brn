package com.epam.brn.service

import com.epam.brn.service.cloud.CloudService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@DisplayName("UrlConversionService test using MockK")
internal class UrlConversionServiceTest {

    @InjectMockKs
    private lateinit var urlConversionService: UrlConversionService

    @MockK
    private lateinit var cloudService: CloudService

    @Test
    fun `should return correct url for subgroup picture`() {
        // GIVEN
        val subGroupCode = "subGroupCode"
        val baseFileUrl = "baseFileUrl"
        every { cloudService.baseFileUrl() } returns (baseFileUrl)
        ReflectionTestUtils.setField(urlConversionService, "folderForThemePictures", "/folderForThemePictures")
        // WHEN
        val makeUrlForNoise = urlConversionService.makeUrlForSubGroupPicture(subGroupCode)

        // THEN
        assertEquals("baseFileUrl/folderForThemePictures/subGroupCode.svg", makeUrlForNoise)
        verify(exactly = 1) { cloudService.baseFileUrl() }
    }

    @Test
    fun `should return correct url for task picture when picture exists in default folder`() {
        // GIVEN
        val word = "word"
        val baseFileUrl = "baseFileUrl"
        val defaultPicturesPath = "pictures/"
        val unverifiedPicturesPath = "pictures/unverified"
        val fullFileName = "$baseFileUrl/${defaultPicturesPath}word.png"
        val extensions: Set<String> = setOf(".png", ".jpg")
        every { cloudService.isFileExist(eq(defaultPicturesPath), any()) } returns true
        every { cloudService.createFullFileName(any(), any()) } returns fullFileName
        ReflectionTestUtils.setField(urlConversionService, "defaultPicturesPath", defaultPicturesPath)
        ReflectionTestUtils.setField(urlConversionService, "unverifiedPicturesPath", unverifiedPicturesPath)
        ReflectionTestUtils.setField(urlConversionService, "pictureExtensions", extensions)

        // WHEN
        val resultPictureUrl = urlConversionService.makeUrlForTaskPicture(word)
        // THEN
        assertEquals(fullFileName, resultPictureUrl)
    }

    @Test
    fun `should return correct url for task picture when picture exists in unverified pictures folder`() {
        // GIVEN
        val word = "word"
        val baseFileUrl = "baseFileUrl"
        val defaultPicturesPath = "pictures/"
        val unverifiedPicturesPath = "pictures/unverified/"
        val fullFileName = "$baseFileUrl/${defaultPicturesPath}word.png"
        val extensions: Set<String> = setOf(".png", ".jpg")
        every { cloudService.isFileExist(eq(defaultPicturesPath), any()) } returns false
        every { cloudService.isFileExist(eq(unverifiedPicturesPath), any()) } returns true
        every { cloudService.createFullFileName(any(), any()) } returns fullFileName
        ReflectionTestUtils.setField(urlConversionService, "defaultPicturesPath", defaultPicturesPath)
        ReflectionTestUtils.setField(urlConversionService, "unverifiedPicturesPath", unverifiedPicturesPath)
        ReflectionTestUtils.setField(urlConversionService, "pictureExtensions", extensions)
        // WHEN
        val resultPictureUrl = urlConversionService.makeUrlForTaskPicture(word)
        // THEN
        assertEquals(fullFileName, resultPictureUrl)
    }

    @Test
    fun `should return emptyString when picture doesn't exists`() {
        // GIVEN
        val word = "word"
        val defaultPicturesPath = "pictures/"
        val unverifiedPicturesPath = "pictures/unverified"
        val extensions: Set<String> = setOf(".png", ".jpg")
        every { cloudService.isFileExist(eq(defaultPicturesPath), any()) } returns false
        every { cloudService.isFileExist(eq(unverifiedPicturesPath), any()) } returns false
        ReflectionTestUtils.setField(urlConversionService, "defaultPicturesPath", defaultPicturesPath)
        ReflectionTestUtils.setField(urlConversionService, "unverifiedPicturesPath", unverifiedPicturesPath)
        ReflectionTestUtils.setField(urlConversionService, "pictureExtensions", extensions)
        // WHEN
        val resultPictureUrl = urlConversionService.makeUrlForTaskPicture(word)
        // THEN
        assertEquals("", resultPictureUrl)
    }

    @Test
    fun `makeUrlForNoise should return baseFileUrl + noiseUrl`() {
        // GIVEN
        val noiseUrl = "noiseUrl"
        val baseFileUrl = "baseFileUrl"
        every { cloudService.baseFileUrl() } returns (baseFileUrl)

        // WHEN
        val makeUrlForNoise = urlConversionService.makeUrlForNoise(noiseUrl)

        // THEN
        assertEquals(baseFileUrl + noiseUrl, makeUrlForNoise)
        verify(exactly = 1) { cloudService.baseFileUrl() }
    }

    @Test
    fun `makeUrlForNoise with noiseUrl = Empty should return Empty`() {
        // GIVEN
        val noiseUrl = ""
        val baseFileUrl = "baseFileUrl"
        every { cloudService.baseFileUrl() } returns (baseFileUrl)

        // WHEN
        val makeUrlForNoise = urlConversionService.makeUrlForNoise(noiseUrl)

        // THEN
        assertEquals("", makeUrlForNoise)
        verify(exactly = 0) { cloudService.baseFileUrl() }
    }

    @Test
    fun `makeUrlForNoise with noiseUrl = Null should return Empty`() {
        // GIVEN
        val noiseUrl = null
        val baseFileUrl = "baseFileUrl"
        every { cloudService.baseFileUrl() } returns (baseFileUrl)

        // WHEN
        val makeUrlForNoise = urlConversionService.makeUrlForNoise(noiseUrl)

        // THEN
        assertEquals("", makeUrlForNoise)
        verify(exactly = 0) { cloudService.baseFileUrl() }
    }
}
