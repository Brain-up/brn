package com.epam.brn.dto

data class SubGroupResponse(
    var seriesId: Long,
    var id: Long,
    var level: Int,
    var name: String,
    var pictureUrl: String,
    var description: String?,
    var withPictures: Boolean,
    val exercises: MutableList<Long?> = mutableListOf()
)
