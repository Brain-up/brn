package com.epam.brn.model

import com.epam.brn.dto.response.DoctorUserAccountDto
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.response.UserWithAnalyticsDto
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
@EntityListeners(AuditingEntityListener::class)
data class UserAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val userId: String? = null,
    @Column(nullable = false, unique = true)
    val email: String?,
    var fullName: String?,
    val password: String?,
    var bornYear: Int?,
    var gender: String?,
    var active: Boolean = true,
    @Column(nullable = false)
    @CreatedDate
    var created: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    @Column(nullable = false)
    @LastModifiedDate
    var changed: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    @LastModifiedBy
    @Column(name = "changed_by")
    var changedBy: String = "",
    var avatar: String? = null,
    var photo: String? = null,
    var description: String? = null,
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    var patients: MutableList<UserAccount> = mutableListOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    var doctor: UserAccount? = null,
    @OneToMany(mappedBy = "userAccount", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var headphones: MutableSet<Headphones> = hashSetOf()
) {
    @ManyToMany(cascade = [(CascadeType.MERGE)])
    @JoinTable(
        name = "user_authorities",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id", referencedColumnName = "id")]
    )
    var authoritySet: MutableSet<Authority> = hashSetOf()

    override fun toString(): String {
        return "UserAccount(id=$id, userId=$userId, fullName='$fullName', email='$email'," +
            " bornYear=$bornYear, gender=$gender, description=$description)"
    }

    fun toDto() = toDto(this.doctor?.toDoctorDto())

    private fun toDto(doctor: DoctorUserAccountDto?) = UserAccountDto(
        id = id,
        userId = userId,
        name = fullName,
        active = active,
        email = email,
        bornYear = bornYear,
        gender = gender?.let { Gender.valueOf(it) },
        created = created,
        changed = changed,
        avatar = avatar,
        photo = photo,
        description = description,
        headphones = headphones
            .map(Headphones::toDto)
            .toHashSet(),
        doctor = doctor
    ).also {
        it.authorities = this.authoritySet
            .map(Authority::authorityName)
            .toMutableSet()
    }

    fun toDoctorDto() = DoctorUserAccountDto(
        id = id,
        userId = userId,
        name = fullName,
        active = active,
        email = email,
        bornYear = bornYear,
        gender = gender?.let { Gender.valueOf(it) },
        created = created,
        changed = changed,
        avatar = avatar,
        photo = photo,
        description = description,
        headphones = headphones
            .map(Headphones::toDto)
            .toMutableSet()
    ).also {
        it.authorities = this.authoritySet
            .map(Authority::authorityName)
            .toMutableSet()
        // avoiding circular back-reference
        it.patients = patients
            .map { patient -> patient.toDto(it) }
            .toMutableList()
    }

    fun toAnalyticsDto() = UserWithAnalyticsDto(
        id = id,
        userId = userId,
        name = fullName,
        active = active,
        email = email,
        bornYear = bornYear,
        gender = gender?.let { Gender.valueOf(it) },
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAccount

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        return result
    }
}
