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
    var id: Long,
    var login: String,
    var name: String?,
    var email: String?,
    var avatarUrl: String?,
    var bio: String?,
    var company: String?,
    var contributions: Long
) {

    fun toDto(): GitHubUserDto = GitHubUserDto(
        id = id,
        login = login,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
        bio = bio,
        company = company,
    )

    override fun toString() =
        "GitHubUser(id=$id, login='$login', name=$name, email=$email, avatarUrl=$avatarUrl, bio=$bio, " +
            "company=$company, contributions=$contributions)"
}
