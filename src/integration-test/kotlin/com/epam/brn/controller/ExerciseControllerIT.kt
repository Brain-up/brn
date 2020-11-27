package com.epam.brn.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import com.epam.brn.BaseIT
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
@ActiveProfiles("integration-tests")
class ExerciseControllerIT : BaseIT() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun shouldReturnCorrect400Code() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/exercises"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
