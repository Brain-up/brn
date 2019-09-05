package com.epam.brn.controller

import com.epam.brn.dto.UserData
import com.epam.brn.service.UserDetailService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserDetailController(val userDetailService: UserDetailService) {

    @GetMapping("/user")
    fun start(@RequestParam(value = "userId", defaultValue = "0") userId: String): UserData {
        val level = userDetailService.getLevel(userId)
        return UserData(userId, level)
    }
}