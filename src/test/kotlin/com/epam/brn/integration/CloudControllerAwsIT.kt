package com.epam.brn.integration

import com.epam.brn.enums.BrnRole
import com.epam.brn.model.Resource
import com.epam.brn.service.ResourceService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

private const val WORD = "fileName"

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@WithMockUser(username = "admin", roles = [BrnRole.ADMIN, BrnRole.USER])
@Tag("integration-test")
@TestPropertySource(properties = ["cloud.provider=aws"])
class CloudControllerAwsIT {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var resourceService: ResourceService

    @BeforeEach
    fun setup() {
        if (resourceService.findFirstResourceByWord(WORD) == null) {
            resourceService.save(Resource(word = WORD, description = "description", wordType = "OBJECT"))
        }
    }

    @Test
    fun `should get correct signature and policy for S3 upload`() {
        val filePath = "tasks/\${filename}"
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .get("/cloud/upload")
                    .queryParam("filePath", filePath)
                    .contentType(MediaType.APPLICATION_JSON),
            )

        // THEN
        val response =
            """{
            "data": {
                "action": "https://somebucket.s3.amazonaws.com",
                "input": [
                  {
                    "policy": "ew0KICAiY29uZGl0aW9ucyIgOiBbIHsNCiAgICAiYnVja2V0IiA6ICJzb21lYnVja2V0Ig0KICB9LCB7DQogICAgImFjbCIgOiAicHJpdmF0ZSINCiAgfSwgWyAic3RhcnRzLXdpdGgiLCAiJGtleSIsICJ0YXNrcy8ke2ZpbGVuYW1lfSIgXSwgew0KICAgICJ4LWFtei1tZXRhLXV1aWQiIDogImM0OTc5MWIyLWIyN2ItNGVkZi1iYWM4LTg3MzQxNjRjMjBlNiINCiAgfSwgew0KICAgICJ4LWFtei1zZXJ2ZXItc2lkZS1lbmNyeXB0aW9uIiA6ICJBRVMyNTYiDQogIH0sIHsNCiAgICAieC1hbXotY3JlZGVudGlhbCIgOiAiQUtJQUk3S0xLQVRXVkNNRUtHUEEvMjAyMDAxMzAvdXMtZWFzdC0yL3MzL2F3czRfcmVxdWVzdCINCiAgfSwgew0KICAgICJ4LWFtei1hbGdvcml0aG0iIDogIkFXUzQtSE1BQy1TSEEyNTYiDQogIH0sIHsNCiAgICAieC1hbXotZGF0ZSIgOiAiMjAyMDAxMzBUMTEzOTE3WiINCiAgfSBdLA0KICAiZXhwaXJhdGlvbiIgOiAiMjAyMC0wMS0zMFQyMTozOToxNy4xMTRaIg0KfQ=="
                  },
                  {
                    "x-amz-signature": "4d39e2b2ac5833352544d379dadad1ffba3148d9936d814f36f50b7af2cd8e8e"
                  },
                  {
                    "key": "$filePath"
                  },
                  {
                    "acl": "private"
                  },
                  {
                    "x-amz-meta-uuid": "c49791b2-b27b-4edf-bac8-8734164c20e6"
                  },
                  {
                    "x-amz-server-side-encryption": "AES256"
                  },
                  {
                    "x-amz-credential": "AKIAI7KLKATWVCMEKGPA/20200130/us-east-2/s3/aws4_request"
                  },
                  {
                    "x-amz-algorithm": "AWS4-HMAC-SHA256"
                  },
                  {
                    "x-amz-date": "20200130T113917Z"
                  }
                ]
            },
            "errors": [],
            "meta": []
        }"""
        resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(response.replace(Regex("\\s"), "")))
    }

    @Test
    fun `should upload allowed file to S3`() {
        val fileName = "$WORD.png"
        val fileData = "some text"
        val file =
            MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                fileData.toByteArray(),
            )
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .multipart("/cloud/upload/picture")
                    .file(file),
            )

        // THEN
        resultAction
            .andExpect(MockMvcResultMatchers.status().isCreated)
    }

    @Test
    fun `should not upload non allowed file by extension file to S3`() {
        val fileName = "$WORD.not-allowed-ext"
        val fileData = "some text"
        val file =
            MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                fileData.toByteArray(),
            )
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .multipart("/cloud/upload/picture")
                    .file(file),
            )

        // THEN
        resultAction
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }
}
