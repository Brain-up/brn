package com.epam.brn.model

import com.epam.brn.dto.ContactDto
import com.epam.brn.enums.ContactType
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table
data class Contact(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Enumerated(EnumType.STRING)
    var type: ContactType = ContactType.EMAIL,
    var value: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Contact

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Contact(id=$id, type=$type, value='$value')"
    }

    fun toDto(): ContactDto {
        return ContactDto(
            type = type.name,
            value = value
        )
    }
}
