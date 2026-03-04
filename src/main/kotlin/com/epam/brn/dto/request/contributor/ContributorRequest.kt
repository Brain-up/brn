package com.epam.brn.dto.request.contributor

import com.epam.brn.enums.ContributorType
import com.epam.brn.model.Contributor
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Length

data class ContributorRequest(
    @field:NotBlank
    @field:Length(max = 255)
    val name: String,
    @field:NotBlank
    @field:Length(max = 255)
    val description: String?,
    @field:Length(max = 255)
    val company: String?,
    @field:NotBlank
    @field:Length(max = 255)
    val nameEn: String?,
    @field:NotBlank
    @field:Length(max = 255)
    val descriptionEn: String?,
    @field:Length(max = 255)
    val companyEn: String?,
    @field:Length(max = 255)
    val pictureUrl: String?,
    @field:Positive
    val contribution: Long,
    val type: ContributorType,
    val active: Boolean,
    val contacts: Set<@Valid ContactRequest> = mutableSetOf(),
) {
    fun toEntity(): Contributor {
        val contributor =
            Contributor(
                name = this.name,
                description = this.description,
                company = this.company,
                nameEn = this.nameEn,
                descriptionEn = this.descriptionEn,
                companyEn = this.companyEn,
                pictureUrl = this.pictureUrl,
                contribution = this.contribution!!,
                type = type!!,
                active = active,
            )
        contributor.contacts = contacts.map { it.toEntity() }.toMutableSet()
        return contributor
    }
}
