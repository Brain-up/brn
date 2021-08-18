package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@DisplayName("UrlConversionService test using MockK")

internal class UrlConversionServiceTest {

    @InjectMockKs
    private lateinit var urlConversionService: UrlConversionService

    @MockK
    private lateinit var awsConfig: AwsConfig

    @Test
    fun `should return correct url for subgroup picture`() {
        // GIVEN
        val subGroupCode = "subGroupCode"
        val baseFileUrl = "baseFileUrl"
        every { awsConfig.baseFileUrl } returns (baseFileUrl)
        ReflectionTestUtils.setField(urlConversionService, "folderForThemePictures", "/folderForThemePictures")
        // WHEN
        val makeUrlForNoise = urlConversionService.makeUrlForSubGroupPicture(subGroupCode)

        // THEN
        assertEquals("baseFileUrl/folderForThemePictures/subGroupCode.svg", makeUrlForNoise)
        verify(exactly = 1) { awsConfig.baseFileUrl }
    }

    @Test
    fun `makeUrlForNoise should return baseFileUrl + noiseUrl`() {
        // GIVEN
        val noiseUrl = "noiseUrl"
        val baseFileUrl = "baseFileUrl"
        every { awsConfig.baseFileUrl } returns (baseFileUrl)

        // WHEN
        val makeUrlForNoise = urlConversionService.makeUrlForNoise(noiseUrl)

        // THEN
        assertEquals(baseFileUrl + noiseUrl, makeUrlForNoise)
        verify(exactly = 1) { awsConfig.baseFileUrl }
    }

    @Test
    fun `makeUrlForNoise with noiseUrl = Empty should return Empty`() {
        // GIVEN
        val noiseUrl = ""
        val baseFileUrl = "baseFileUrl"
        every { awsConfig.baseFileUrl } returns (baseFileUrl)

        // WHEN
        val makeUrlForNoise = urlConversionService.makeUrlForNoise(noiseUrl)

        // THEN
        assertEquals("", makeUrlForNoise)
        verify(exactly = 0) { awsConfig.baseFileUrl }
    }

    @Test
    fun `makeUrlForNoise with noiseUrl = Null should return Empty`() {
        // GIVEN
        val noiseUrl = null
        val baseFileUrl = "baseFileUrl"
        every { awsConfig.baseFileUrl } returns (baseFileUrl)

        // WHEN
        val makeUrlForNoise = urlConversionService.makeUrlForNoise(noiseUrl)

        // THEN
        assertEquals("", makeUrlForNoise)
        verify(exactly = 0) { awsConfig.baseFileUrl }
    }
}
