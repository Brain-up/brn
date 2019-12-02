package com.epam.brn.model

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.UserAccountDto
import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
data class UserAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val userName: String,
    @Column(nullable = false)
    val email: String,
    @Column(nullable = false)
    val password: String,
    val active: Boolean,
    val birthDate: LocalDate? = null
) {
    @OneToMany(cascade = [(CascadeType.ALL)])
    val phoneNumbers: List<PhoneNumber>? = null
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "progress_id")
    val progress: Progress? = null
    @OneToMany(mappedBy = "userAccount", cascade = [CascadeType.ALL])
    var authoritySet: MutableSet<Authority> = hashSetOf()

    override fun toString(): String {
        return "UserAccount(id=$id, name='$userName', email='$email', birthDate=$birthDate, phoneNumbers=$phoneNumbers, progress=$progress)"
    }

    fun toDto() = UserAccountDto(
        id = this.id,
        userName = this.userName,
        active = this.active,
        email= this.email,
        birthDate = this.birthDate
    )
}