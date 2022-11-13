package com.epam.brn.model

import com.epam.brn.dto.response.ContributorDetailsResponse
import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.enums.ContributorType
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
class Contributor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
    var description: String? = null,
    var company: String? = null,
    var nameEn: String? = null,
    var descriptionEn: String? = null,
    var companyEn: String? = null,
    @Enumerated(EnumType.STRING)
    var type: ContributorType = ContributorType.DEVELOPER,
    var pictureUrl: String? = null,
    var contribution: Long = 0,
    var active: Boolean = true,
) {

    @OneToOne(cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "github_id", referencedColumnName = "id")
    var gitHubUser: GitHubUser? = null

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "contributor_id")
    var contacts: MutableSet<Contact> = mutableSetOf()

    fun toContributorResponse(locale: String = "ru-ru"): ContributorResponse {
        val dto = ContributorResponse(
            id = id!!,
            gitHubLogin = gitHubUser?.login ?: "",
            name = name,
            nameEn = nameEn,
            company = company,
            companyEn = companyEn,
            description = description,
            descriptionEn = descriptionEn,
            pictureUrl = pictureUrl,
            contribution = contribution,
            type = type,
            active = active,
            contacts = contacts.map {
                it.toDto()
            }.toSet()
        )
        return dto
    }

    fun toContributorDetailsDto(): ContributorDetailsResponse {
        return ContributorDetailsResponse(
            id = id!!,
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
        "Contributor(id=$id, name=$name, description=$description, company=$company, nameEn=$nameEn, " +
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
}
