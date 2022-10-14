package com.epam.brn.model

import com.epam.brn.dto.response.RoleResponse
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true, nullable = false, name = "AUTHORITY_NAME")
    val name: String

) {
    fun toDto() = RoleResponse(
        name = name
    )

    override fun toString(): String {
        return "Role(id=$id, name='$name')"
    }
}
