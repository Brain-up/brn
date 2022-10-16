package com.epam.brn.model

import com.epam.brn.dto.response.AuthorityResponse
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Authority(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true, nullable = false, name = "AUTHORITY_NAME")
    val authorityName: String

) {
    fun toDto() = AuthorityResponse(
        authorityName = authorityName
    )

    override fun toString(): String {
        return "Authority(id=$id, authority='$authorityName')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Authority

        if (id != other.id) return false
        if (authorityName != other.authorityName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + authorityName.hashCode()
        return result
    }
}
