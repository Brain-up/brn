package com.epam.brn.model

import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.enums.Gender
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToOne

@Entity
data class UserAccount(
    @Id
    @GeneratedValue(generator = "user_account_id_seq", strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var fullName: String,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    val password: String,
    var bornYear: Int,
    var gender: String,
    var active: Boolean = true,
    @Column(nullable = false)
    var created: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    @Column(nullable = false)
    var changed: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    var avatar: String? = null
) {
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

    fun toDto(): UserAccountDto {
        val userAccountDto = UserAccountDto(
            id = this.id,
            name = this.fullName,
            active = this.active,
            email = this.email,
            bornYear = this.bornYear,
            gender = Gender.valueOf(gender),
            created = created,
            changed = changed,
            avatar = avatar
        )
        userAccountDto.authorities = this.authoritySet
            .map(Authority::authorityName)
            .toMutableSet()
        return userAccountDto
    }
}
