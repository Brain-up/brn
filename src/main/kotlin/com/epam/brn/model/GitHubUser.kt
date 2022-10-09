package com.epam.brn.model

import com.epam.brn.dto.GitHubUserDto
import org.hibernate.annotations.DynamicUpdate
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "github_user")
@DynamicUpdate
class GitHubUser(
    @Id
    var id: Long? = null,
    var login: String,
    var name: String,
    var email: String,
    var avatarUrl: String,
    var bio: String,
    var company: String,
    var contributions: Long
) {

    fun toDto(): GitHubUserDto = GitHubUserDto(
        id = id!!,
        login = login,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
        bio = bio,
        company = company,
    )

    override fun toString() =
        "Exercise(id=$id, login='$login', name=$name, email=$email, avatarUrl=$avatarUrl, bio=$bio, " +
            "company=$company, contributions=$contributions)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GitHubUser

        if (id != other.id) return false
        if (login != other.login) return false
        if (name != other.name) return false
        if (email != other.email) return false
        if (avatarUrl != other.avatarUrl) return false
        if (bio != other.bio) return false
        if (company != other.company) return false
        if (contributions != other.contributions) return false

        return true
    }
}
