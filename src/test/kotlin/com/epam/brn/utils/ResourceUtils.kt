package com.epam.brn.utils

import com.epam.brn.enums.WordType
import com.epam.brn.model.Resource

fun resource(name: String): Resource {
    return Resource(
        word = name,
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/test/$name.ogg",
        pictureFileUrl = "pictures/$name.jpg",
        locale = "ru-ru"
    )
}
