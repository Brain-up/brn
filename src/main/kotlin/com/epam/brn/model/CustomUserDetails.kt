package com.epam.brn.model

import java.lang.Boolean.TRUE
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(userAccount: UserAccount) : UserDetails {

    private val userName: String = userAccount.email
    private val password: String? = userAccount.password
    private val active = userAccount.active
    private val authorities: List<GrantedAuthority>

    init {
        authorities = userAccount.authoritySet
            .map { it.authorityName }
            .map { SimpleGrantedAuthority(it) }
    }

    override fun getAuthorities() = this.authorities.toMutableList()

    override fun isEnabled() = this.active

    override fun getUsername() = this.userName

    override fun isCredentialsNonExpired(): Boolean = TRUE

    override fun getPassword() = this.password

    override fun isAccountNonExpired(): Boolean = TRUE

    override fun isAccountNonLocked(): Boolean = TRUE
}
