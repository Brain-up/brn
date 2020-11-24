package com.epam.brn.model

import com.epam.brn.dto.response.UserAccountResponse
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val fullName: String,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    val password: String,
    val bornYear: Int,
    val gender: String,
    val active: Boolean = true,
    @Column(nullable = false)
    var created: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    @Column(nullable = false)
    val changed: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    val avatar: String? = null
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

    override fun toString(): String {
        return "UserAccount(id=$id, fullName='$fullName', email='$email', bornYear=$bornYear, gender=$gender,  progress=$progress)"
    }

    fun toDto(): UserAccountResponse {
        val userAccountDto = UserAccountResponse(
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
