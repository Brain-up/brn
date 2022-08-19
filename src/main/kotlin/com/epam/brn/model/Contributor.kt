package com.epam.brn.model

import com.epam.brn.dto.ContributorAdminDto
import com.epam.brn.dto.ContributorUserDto
import org.hibernate.annotations.DynamicUpdate
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table
@DynamicUpdate
data class Contributor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String? = null,
    var description: String? = null,
    var company: String? = null,
    var nameEn: String? = null,
    var descriptionEn: String? = null,
    var companyEn: String? = null,
    @Enumerated(EnumType.STRING)
    var type: ContributorType = ContributorType.DEVELOPER,
    var pictureUrl: String? = null,
    var contribution: Long? = null,
    var active: Boolean = true,
) {

    @OneToOne(cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "github_id", referencedColumnName = "id")
    var gitHubUser: GitHubUser? = null

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "contributor_id")
    var contacts: MutableSet<Contact> = mutableSetOf()

    fun toContributorUserDto(locale: String = "ru-ru"): ContributorUserDto {
        if (locale == "ru-ru") {
            return ContributorUserDto(
                id = id,
                name = name ?: gitHubUser?.name,
                description = description ?: gitHubUser?.bio,
                company = company ?: gitHubUser?.company,
                pictureUrl = pictureUrl,
                contacts = contacts.map {
                    it.toDto()
                }.toSet()
            )
        } else {
            return ContributorUserDto(
                id = id,
                name = nameEn ?: gitHubUser?.name,
                description = descriptionEn ?: gitHubUser?.bio,
                company = companyEn ?: gitHubUser?.company,
                pictureUrl = pictureUrl,
                contacts = contacts.map {
                    it.toDto()
                }.toSet()
            )
        }
    }

    fun toContributorAdminDto(): ContributorAdminDto {
        return ContributorAdminDto(
            id = id,
            type = type.name,
            name = name,
            description = description,
            company = company,
            nameEn = nameEn,
            descriptionEn = descriptionEn,
            companyEn = companyEn,
            pictureUrl = pictureUrl,
            active = active,
            gitHubUser = gitHubUser?.toDto(),
            contacts = contacts.map {
                it.toDto()
            }.toSet(),
        )
    }

    override fun toString() =
        "Exercise(id=$id, name=$name, description=$description, company=$company, nameEn=$nameEn, " +
            "descriptionEn=$descriptionEn, companyEn=$companyEn, type=$type, pictureUrl=$pictureUrl," +
            "contribution=$contribution, gitHubUser=$gitHubUser)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contributor

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (company != other.company) return false
        if (nameEn != other.nameEn) return false
        if (descriptionEn != other.descriptionEn) return false
        if (companyEn != other.companyEn) return false
        if (type != other.type) return false
        if (pictureUrl != other.pictureUrl) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (company?.hashCode() ?: 0)
        result = 31 * result + (nameEn?.hashCode() ?: 0)
        result = 31 * result + (descriptionEn?.hashCode() ?: 0)
        result = 31 * result + (companyEn?.hashCode() ?: 0)
        result = 31 * result + (pictureUrl?.hashCode() ?: 0)
        return result
    }
}
