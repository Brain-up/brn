package com.epam.brn.auth.model

import com.epam.brn.model.UserAccount
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.lang.Boolean.TRUE

class CustomUserDetails(userAccount: UserAccount) : UserDetails {

    private val userName: String? = userAccount.email
    private val active = userAccount.active
    private val authorities: List<GrantedAuthority>

    init {
        authorities = userAccount.roleSet
            .map { "ROLE_$it.name" }
            .map { SimpleGrantedAuthority(it) }
    }

    override fun getAuthorities() = this.authorities.toMutableList()

    override fun isEnabled() = this.active

    override fun getUsername() = this.userName

    override fun isCredentialsNonExpired(): Boolean = TRUE

    override fun getPassword() = null

    override fun isAccountNonExpired(): Boolean = TRUE

    override fun isAccountNonLocked(): Boolean = TRUE
}
