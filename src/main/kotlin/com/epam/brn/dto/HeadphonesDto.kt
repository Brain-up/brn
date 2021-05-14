package com.epam.brn.dto

import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Headphones
import javax.validation.constraints.NotBlank

data class HeadphonesDto(
    var id: Long? = null,
    @NotBlank
    var name: String = "",
    var type: HeadphonesType,
    var description: String = "",
    var userAccount: Long? = null
) {
    fun toEntity() = Headphones(
        id = id,
        name = name,
        type = type,
        description = description
    )
}
