package com.epam.brn.integration

import com.epam.brn.constant.BrnPath
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
                    "policy": "ew0KICAiY29uZGl0aW9ucyIgOiBbIHsNCiAgICAiYnVja2V0IiA6ICJzb21lYnVja2V0Ig0KICB9LCB7DQogICAgImFjbCIgOiAicHJpdmF0ZSINCiAgfSwgWyAic3RhcnRzLXdpdGgiLCAiJGtleSIsICJ0YXNrcy8iIF0sIHsNCiAgICAieC1hbXotbWV0YS11dWlkIiA6ICJjNDk3OTFiMi1iMjdiLTRlZGYtYmFjOC04NzM0MTY0YzIwZTYiDQogIH0sIHsNCiAgICAieC1hbXotc2VydmVyLXNpZGUtZW5jcnlwdGlvbiIgOiAiQUVTMjU2Ig0KICB9LCB7DQogICAgIngtYW16LWNyZWRlbnRpYWwiIDogIkFLSUFJN0tMS0FUV1ZDTUVLR1BBLzIwMjAwMTMwL3VzLWVhc3QtMi9zMy9hd3M0X3JlcXVlc3QiDQogIH0sIHsNCiAgICAieC1hbXotYWxnb3JpdGhtIiA6ICJBV1M0LUhNQUMtU0hBMjU2Ig0KICB9LCB7DQogICAgIngtYW16LWRhdGUiIDogIjIwMjAwMTMwVDExMzkxN1oiDQogIH0gXSwNCiAgImV4cGlyYXRpb24iIDogIjIwMjAtMDEtMzBUMjE6Mzk6MTcuMTE0WiINCn0="
                  },
                  {
                    "x-amz-signature": "5ed1002da60d3ad165667e04a95f7b1d75d13438fddab0d3a87f173d5a7fb4fc"
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
