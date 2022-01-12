package com.epam.brn.dto.request

import com.fasterxml.jackson.annotation.JsonCreator

data class SubGroupChangeRequest @JsonCreator internal constructor(
    val withPictures: Boolean
)
