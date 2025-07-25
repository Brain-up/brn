package com.epam.brn.model

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.enums.BrnGender
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
class UserAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var userId: String? = null,
    @Column(nullable = false, unique = true)
    val email: String?,
    var fullName: String?,
    var bornYear: Int? = null,
    var gender: String? = null,
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
    @Column(name = "last_visit")
    var lastVisit: LocalDateTime? = null,
    var avatar: String? = null,
    var photo: String? = null,
    var description: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    var doctor: UserAccount? = null,
    @OneToMany(mappedBy = "userAccount", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var headphones: MutableSet<Headphones> = hashSetOf(),
) {
    var password: String? = null

    @Column(name = "is_firebase_error")
    var isFirebaseError: Boolean = false

    @ManyToMany(cascade = [(CascadeType.MERGE)])
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
    )
    var roleSet: MutableSet<Role> = hashSetOf()

    fun toDto() = UserAccountDto(
        id = id,
        userId = userId,
        name = fullName,
        active = active,
        email = email,
        bornYear = bornYear,
        gender = gender?.let { BrnGender.valueOf(it) },
        created = created,
        changed = changed,
        avatar = avatar,
        photo = photo,
        description = description,
        headphones =
            headphones
                .map(Headphones::toDto)
                .toHashSet(),
        doctorId = doctor?.id,
    ).also {
        it.roles =
            this.roleSet
                .map(Role::name)
                .toMutableSet()
    }

    fun toAnalyticsDto() = UserWithAnalyticsResponse(
        id = id,
        userId = userId,
        name = fullName,
        active = active,
        email = email,
        bornYear = bornYear,
        gender = gender?.let { BrnGender.valueOf(it) },
        lastVisit = lastVisit ?: created,
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
