package com.epam.brn.integration

import com.epam.brn.config.AwsConfig
import com.epam.brn.constant.BrnPath
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@WithMockUser(username = "admin", roles = ["ADMIN"])
@Tag("integration-test")
@TestPropertySource(properties = ["cloud.provider=aws"])
class CloudControllerAwsIT {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun awsConfig(
            @Value("\${cloud.expireAfterDuration}") expireAfterDuration: String,
            @Value("\${aws.accessRuleCanned}") accessRuleCanned: String,
            @Value("\${aws.credentialsPath}") credentialsPath: String,
            @Value("\${aws.accessKeyId}") accessKeyIdProperty: String,
            @Value("\${aws.secretAccessKey}") secretAccessKeyProperty: String,
            @Value("\${aws.region}") region: String
        ): AwsConfig {
            return object : AwsConfig(expireAfterDuration, accessRuleCanned, credentialsPath, accessKeyIdProperty, secretAccessKeyProperty, region) {
                override fun instant(): OffsetDateTime = Instant.ofEpochMilli(1580384357114).atOffset(ZoneOffset.UTC)
                override fun uuid(): String = "c49791b2-b27b-4edf-bac8-8734164c20e6"
            }
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should get correct signature and policy for S3 upload`() {
        val filePath = "tasks/\${filename}"
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("${BrnPath.CLOUD}${BrnPath.UPLOAD}").queryParam("filePath", filePath)
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        val response = """{
            "data": {
                "action": "http://somebucket.s3.amazonaws.com",
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
}
