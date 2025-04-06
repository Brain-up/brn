package com.epam.brn.service.cloud

import java.io.InputStream

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
    fun uploadFile(
        path: String,
        fileName: String,
        inputStream: InputStream,
    )

    /**
     * Create folder in cloud storage
     */
    fun createFolder(folderPath: String)

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
    fun getStorageFolders(): List<String>

    /**
     * Get list of files in specified folder
     */
    fun getFileNames(folderPath: String): List<String>

    /**
     * Get map of file paths with file name as key.
     */
    fun getFilePathMap(folderPath: String): Map<String, String>

    /**
     * Delete specified files
     */
    fun deleteFiles(fileNames: List<String>)

    /**
     * Check that file is exist in cloud storage
     */
    fun isFileExist(
        filePath: String,
        fileName: String,
    ): Boolean

    /**
     * Create full name of file
     */
    fun createFullFileName(
        path: String,
        filename: String,
    ): String
}
