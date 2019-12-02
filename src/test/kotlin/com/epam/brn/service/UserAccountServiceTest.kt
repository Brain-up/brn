package com.epam.brn.service

import com.epam.brn.repo.UserAccountRepository
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class UserAccountServiceTest {

    @InjectMocks
    lateinit var userAccountService: UserAccountService

    @Mock
    lateinit var userAccountRepository: UserAccountRepository

    @Test
    @Disabled
    fun `should insert user`() {
        // TODO write a test with new service methods
/*        // GIVEN
        val id = 1L
        val name = "Name"
        val email = "email@email.ru"
        val phone = "+7911111111"
        val userDetails = UserDetails(id, name, email, phone)
        Mockito.`when`(userDetailsRepository.save(userDetails)).thenReturn(userDetails)

        // WHEN
        val newUserId = userDetailsService.addUser(name, email, phone)

        // THEN
        verify(userDetailsRepository, times(1)).save(userDetails)
        assertEquals(id, newUserId)*/
    }
}