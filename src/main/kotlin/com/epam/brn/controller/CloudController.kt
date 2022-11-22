package com.epam.brn.controller

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.CloudUploadService
import com.epam.brn.service.cloud.CloudService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.annotation.security.RolesAllowed

/**
 * Provides form parameters for client direct file upload to cloud and url for client to list bucket contents
 * Documentation https://github.com/Brain-up/brn/wiki/Cloud-file-resource-loading
 */
@RestController
@RequestMapping("/cloud")
@Api(value = "/cloud", tags = ["Cloud"], description = "Contains actions for cloud upload and bucket listing")
@ConditionalOnProperty(name = ["cloud.provider"])
@RolesAllowed(BrnRole.USER)
class CloudController(
    @Autowired private val cloudService: CloudService,
    @Autowired private val cloudUploadService: CloudUploadService
) {

    @GetMapping("/upload")
    @ApiOperation("Get cloud upload form")
    @RolesAllowed(BrnRole.ADMIN)
    @Throws(Exception::class)
    fun signatureForClientDirectUpload(@RequestParam filePath: String?): ResponseEntity<BrnResponse<Map<String, Any>>> {
        if (filePath.isNullOrEmpty())
            throw IllegalArgumentException("File path should be non empty")
        val signedForm = cloudService.uploadForm(filePath)
        return ResponseEntity.ok(BrnResponse(signedForm))
    }

    @GetMapping("/url")
    @ApiOperation("Get cloud bucket url")
    @Throws(Exception::class)
    fun bucketUrl(): ResponseEntity<BrnResponse<String>> =
        ResponseEntity.ok(BrnResponse(cloudService.bucketUrl()))

    @GetMapping("/baseFileUrl")
    @ApiOperation("Get cloud base file url")
    @Throws(Exception::class)
    fun baseFileUrl(): ResponseEntity<BrnResponse<String>> =
        ResponseEntity.ok(BrnResponse(cloudService.baseFileUrl()))

    @GetMapping("/folders")
    @ApiOperation("Get cloud folder structure")
    @RolesAllowed(BrnRole.ADMIN)
    @Throws(Exception::class)
    fun listBucket(): ResponseEntity<BrnResponse<List<String>>> =
        ResponseEntity.ok(BrnResponse(cloudService.getStorageFolders()))

    @PostMapping(value = ["/upload/picture"], consumes = [ MediaType.MULTIPART_FORM_DATA_VALUE ])
    @ApiOperation("Load unverified picture file to cloud storage")
    fun loadUnverifiedPicture(
        @RequestParam(value = "file") multipartFile: MultipartFile
    ): ResponseEntity<BrnResponse<Any>> {
        cloudUploadService.uploadUnverifiedPictureFile(multipartFile)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping(value = ["/upload/contributor/picture"], consumes = [ MediaType.MULTIPART_FORM_DATA_VALUE ])
    @ApiOperation("Upload picture of contributor")
    @RolesAllowed(BrnRole.ADMIN)
    fun uploadContributorPicture(
        @RequestParam(value = "file") multipartFile: MultipartFile,
        @RequestParam(value = "fileName") fileName: String
    ): ResponseEntity<BrnResponse<String>> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BrnResponse(cloudUploadService.uploadContributorPicture(multipartFile, fileName)))
    }
}
