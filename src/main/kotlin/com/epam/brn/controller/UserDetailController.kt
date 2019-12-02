package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.UserData
import com.epam.brn.model.UserAccount
import com.epam.brn.service.UserAccountService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.USERS)
@Api(value = BrnPath.USERS, description = "Contains actions over user details and accounts")
class UserDetailController(@Autowired val userAccountService: UserAccountService) {

    @GetMapping("/findUser")
    fun findUserByName(@RequestParam(value = "name", defaultValue = "0") name: String): UserAccount {
        return userAccountService.findUserByName(name)
    }

    @PostMapping("/addUser")
    fun addUser(
        @RequestParam("name") name: String,
        @RequestParam("email") email: String,
        @RequestParam("phone") phone: String
    ) {
        // Not implemented
    }
}