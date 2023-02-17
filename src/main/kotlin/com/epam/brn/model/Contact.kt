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
class Contact(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Enumerated(EnumType.STRING)
    var type: ContactType = ContactType.EMAIL,
    var value: String
) {

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
