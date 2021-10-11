package com.epam.brn.model

import com.epam.brn.dto.response.AuthorityDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Authority(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true, nullable = false, name = "AUTHORITY_NAME")
    val authorityName: String

) {
    fun toDto() = AuthorityDto(
        authorityName = authorityName
    )

    override fun toString(): String {
        return "Authority(id=$id, authority='$authorityName')"
    }
}
