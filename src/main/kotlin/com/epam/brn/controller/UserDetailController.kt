package com.epam.brn.controller

import com.epam.brn.constant.BrnParams
import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.dto.UserData
import com.epam.brn.model.UserAccount
import com.epam.brn.service.UserAccountService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping(BrnPath.USERS)
@Api(value = BrnPath.USERS, description = "Contains actions over user details and accounts")
class UserDetailController(@Autowired val userAccountService: UserAccountService) {

    @GetMapping
    @ApiOperation("Get user by username")
    fun findUserByName(@RequestParam(BrnParams.USER_NAME) userName: String): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = listOf(userAccountService.findUserByName(userName))))
    }

    @PostMapping
    fun addUser(@Validated @RequestBody userAccountDto: UserAccountDto): ResponseEntity<UserAccountDto> {
        //TODO implement adding new user and update com.epam.brn.dto.UserAccountDto
        return ResponseEntity.status(HttpStatus.CREATED).body(null)
    }
}