package com.epam.brn.model

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.enums.HeadphonesType
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.constraints.NotNull

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["name", "userId"])])
data class Headphones(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @NotNull
    var name: String = "",
    var active: Boolean = true,
    @Enumerated(EnumType.STRING)
    var type: HeadphonesType,
    var description: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    var userAccount: UserAccount? = null
) {
    fun toDto() = HeadphonesDto(
        id,
        name = name,
        active = active,
        description = description,
        type = type,
        userAccount = userAccount?.id
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Headphones

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "Headphones(id=$id, name='$name', type=$type, description='$description')"
    }
}
