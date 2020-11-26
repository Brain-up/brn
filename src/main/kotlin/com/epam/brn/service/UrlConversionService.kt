package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import org.springframework.stereotype.Service

@Service
class UrlConversionService(private val awsConfig: AwsConfig) {
    fun makeFullUrl(url: String?): String {
        if (url.isNullOrEmpty())
            return ""
        return awsConfig.baseFileUrl + url
    }
}
