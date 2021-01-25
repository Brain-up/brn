package com.epam.brn.dto

data class SubGroupDto(
    var seriesId: Long,
    var id: Long,
    var level: Int,
    var name: String,
    var pictureUrl: String,
    var description: String?,
    val exercises: MutableList<Long?> = mutableListOf()
)
