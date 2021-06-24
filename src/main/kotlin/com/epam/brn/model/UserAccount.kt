package com.epam.brn.model

import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.response.UserWithAnalyticsDto
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
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
    var created: LocalDateTime? = null,
    @Column(nullable = false)
    @LastModifiedDate
    var changed: LocalDateTime? = null,
    @LastModifiedBy
    @Column(name = "changed_by")
    var changedBy: String = "",
    var avatar: String? = null,
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
        return "UserAccount(id=$id, userId=$userId, fullName='$fullName', email='$email', bornYear=$bornYear, gender=$gender)"
    }

    fun toDto(): UserAccountDto {
        val userAccountDto = UserAccountDto(
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
            headphones = this.headphones.map(Headphones::toDto).toHashSet()
        )
        userAccountDto.authorities = this.authoritySet
            .map(Authority::authorityName)
            .toMutableSet()
        return userAccountDto
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
