package com.epam.brn.service

interface CloudService {
    /**
     * Returns json representation of post request form to be sent to cloud,
     * one of the keys in json is "action" which represents form action,
     * other is "input" which is a list of form inputs
     * "input" key may or may not be present in return value
     */
    fun signatureForClientDirectUpload(filePath: String): Map<String, Any>

    /**
     * Returns url to be used by frontend to list bucket contents
     */
    fun bucketUrl(): String

    fun listBucket(): List<String>
}
