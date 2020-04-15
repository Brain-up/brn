package com.epam.brn.integration

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@TestPropertySource(properties = ["cloud.provider=google"])
class CloudControllerGoogleIT {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should get correct URL for upload`() {
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/cloud/upload")
                .queryParam("filePath", "fileNameOne")
                .contentType(MediaType.APPLICATION_JSON)
        )
        resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(object : BaseMatcher<String>() {
                override fun describeTo(description: Description?) {
                    description?.appendText("Should contain google query parameters")
                }

                override fun matches(actual: Any?): Boolean {
                    val actualString = actual.toString()
                    return (actualString.contains("X-Goog-Algorithm") &&
                            actualString.contains("X-Goog-Credential") &&
                            actualString.contains("X-Goog-Date") &&
                            actualString.contains("X-Goog-Expires") &&
                            actualString.contains("X-Goog-SignedHeaders") &&
                            actualString.contains("X-Goog-Signature"))
                }
            }))
    }
}
