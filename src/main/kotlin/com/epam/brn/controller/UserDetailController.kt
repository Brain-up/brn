package com.epam.brn.controller

import com.epam.brn.dto.UserData
import com.epam.brn.model.UserDetails
import com.epam.brn.service.UserDetailsService
import com.lifescience.brn.constant.BrnPath
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.USER)
@Api(value = BrnPath.USER, description = "")
class UserDetailController(@Autowired val userDetailService: UserDetailsService) {

    @GetMapping("/getLevel")
    @ApiOperation("Get level for user")
    fun getLevel(@RequestParam(value = "userId", defaultValue = "0") userId: String): UserData {
        val level = userDetailService.getLevel(userId)
        return UserData(userId, level)
    }

    @GetMapping("/findUser")
    fun findUserByName(@RequestParam(value = "name", defaultValue = "0") name: String): UserDetails? {
        val user = userDetailService.findUserDetails(name)
        return user
    }

    @PostMapping("/addUser")
    fun addUser(
        @RequestParam("name") name: String,
        @RequestParam("email") email: String,
        @RequestParam("phone") phone: String
    ) {
        userDetailService.addUser(name, email, phone)
    }
}