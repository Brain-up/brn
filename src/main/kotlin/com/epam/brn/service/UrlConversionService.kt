package com.epam.brn.service

import com.epam.brn.service.cloud.CloudService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UrlConversionService(private val cloudService: CloudService) {
    @Value("\${aws.folderForThemePictures}")
    private lateinit var folderForThemePictures: String

    fun makeUrlForNoise(noiseUrl: String?): String {
        return if (noiseUrl.isNullOrEmpty()) ""
        else cloudService.baseFileUrl() + noiseUrl
    }

    fun makeUrlForSubGroupPicture(subGroupCode: String): String =
        cloudService.baseFileUrl() + folderForThemePictures + "/" + subGroupCode + ".svg"
}
