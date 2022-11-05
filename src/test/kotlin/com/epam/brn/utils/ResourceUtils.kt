package com.epam.brn.utils

import com.epam.brn.model.Resource
import com.epam.brn.model.WordType

fun resource(name: String): Resource {
    return Resource(
        word = name,
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/test/$name.ogg",
        pictureFileUrl = "pictures/$name.jpg",
        locale = "ru-ru"
    )
}
