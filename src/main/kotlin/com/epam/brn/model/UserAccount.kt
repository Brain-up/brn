package com.epam.brn.model

import com.epam.brn.dto.UserAccountDto
import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
data class UserAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val firstName: String,
    @Column(nullable = true)
    val lastName: String,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    val password: String?,
    val birthday: LocalDate? = null,
    val active: Boolean
) {
    @OneToMany(cascade = [(CascadeType.ALL)])
    val phoneNumbers: List<PhoneNumber>? = null
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "progress_id")
    val progress: Progress? = null
    @ManyToMany(cascade = [(CascadeType.MERGE)])
    @JoinTable(
        name = "user_authorities",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id", referencedColumnName = "id")]
    )
    var authoritySet: MutableSet<Authority> = hashSetOf()

    override fun toString(): String {
        return "UserAccount(id=$id, firstName='$firstName', lastName='$lastName', email='$email', birthday=$birthday, progress=$progress)"
    }

    fun toDto(): UserAccountDto {
        val userAccountDto = UserAccountDto(
            id = this.id,
            firstName = this.firstName,
            lastName = this.lastName,
            active = this.active,
            email = this.email,
            birthday = this.birthday,
            password = null
        )
        userAccountDto.authorities = this.authoritySet
            .map(Authority::authorityName)
            .toMutableSet()
        return userAccountDto
    }
}
