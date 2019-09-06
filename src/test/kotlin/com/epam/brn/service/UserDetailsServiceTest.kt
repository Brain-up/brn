package com.epam.brn.service

import com.epam.brn.model.UserDetails
import com.epam.brn.repo.UserDetailsRepository
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class UserDetailsServiceTest {

    @InjectMocks
    lateinit var userDetailsService: UserDetailsService

    @Mock
    lateinit var userDetailsRepository: UserDetailsRepository

    @Test
    fun `should insert user`() {
        // GIVEN
        val id = 1
        val name = "Name"
        val email = "email@email.ru"
        val phone = "+7911111111"
        val userDetails = UserDetails(id, name, email, phone)
        Mockito.`when`(userDetailsRepository.save(userDetails)).thenReturn(userDetails)

        // WHEN
        val newUserId = userDetailsService.addUser(name, email, phone)

        // THEN
        verify(userDetailsRepository, times(1)).save(userDetails)
        assertEquals(id, newUserId)
    }
}