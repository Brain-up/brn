package com.epam.brn.model

import org.apache.commons.collections4.CollectionUtils
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.lang.Boolean.TRUE

class CustomUserDetails(userAccount: UserAccount) : UserDetails {

    private val userName: String = userAccount.userName
    private val password: String = userAccount.password
    private val active = userAccount.active
    private val authorities: List<GrantedAuthority>

    init {
        authorities = CollectionUtils.emptyIfNull(userAccount.authoritySet)
            .map { it.authority }
            .map { SimpleGrantedAuthority(it) }
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return this.authorities.toMutableList()
    }

    override fun isEnabled(): Boolean {
        return this.active
    }

    override fun getUsername(): String {
        return this.userName
    }

    override fun isCredentialsNonExpired(): Boolean {
        return TRUE
    }

    override fun getPassword(): String {
        return this.password
    }

    override fun isAccountNonExpired(): Boolean {
        return TRUE
    }

    override fun isAccountNonLocked(): Boolean {
        return TRUE
    }

}

