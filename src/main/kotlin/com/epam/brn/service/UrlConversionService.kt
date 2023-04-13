package com.epam.brn.service

import com.epam.brn.service.cloud.CloudService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UrlConversionService(private val cloudService: CloudService) {
    @Value("\${aws.folderForThemePictures}")
    private lateinit var folderForThemePictures: String

    private val log = logger()

    fun makeUrlForNoise(noiseUrl: String?): String {
        if (noiseUrl.isNullOrEmpty())
            return ""
        return cloudService.baseFileUrl() + noiseUrl
    }

    fun makeUrlForSubGroupPicture(subGroupCode: String): String =
        cloudService.baseFileUrl() + folderForThemePictures + "/" + subGroupCode + ".svg"
}
