package com.epam.brn.cloud

import org.springframework.web.multipart.MultipartFile
import java.io.File

interface CloudService {

    /**
     * Returns json representation of post request form to be sent to cloud,
     * one of the keys in json is "action" which represents form action,
     * other is "input" which is a list of form inputs
     * "input" key may or may not be present in return value
     */
    fun uploadForm(filePath: String): Map<String, Any>

    /**
     * Upload multipart file to cloud storage
     */
    fun uploadFile(filePath: String, fileName: String?, multipartFile: MultipartFile, isVerified: Boolean = true)

    /**
     * Upload file to cloud storage
     */
    fun uploadFile(filePath: String, fileName: String?, file: File, isVerified: Boolean = true)

    /**
     * Create folder in cloud storage
     */
    fun createFolder(folderPath: String)

    /**
     * Check if folder exists in cloud storage
     */
    fun isFolderExists(folderPath: String): Boolean

    /**
     * Returns url to be used by frontend to list bucket contents
     */
    fun bucketUrl(): String

    /**
     * Return base url for cloud storage
     */
    fun baseFileUrl(): String

    /**
     * Get all folders in cloud storage
     */
    fun getListFolder(): List<String>
}
