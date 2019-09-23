package com.epam.brn.controller

import com.epam.brn.service.UserDetailsService
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class UserDetailControllerTest {

    @InjectMocks
    lateinit var userDetailController: UserDetailController

    @Mock
    lateinit var userDetailsService: UserDetailsService

    @Test
    fun `should insert user`() {
        // GIVEN
        val name = "Name"
        val email = "email@email.ru"
        val phone = "+7911111111"
        `when`(userDetailsService.addUser(name, email, phone)).thenReturn(1)

        // WHEN
        userDetailController.addUser(name, email, phone)

        // THEN
        verify(userDetailsService, times(1)).addUser(name, email, phone)
    }
}