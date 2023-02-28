package com.epam.brn.utils

import com.epam.brn.enums.WordType
import com.epam.brn.model.Resource

fun resource(name: String, pictureFileUrl: String = ""): Resource {
    return Resource(
        word = name,
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/test/$name.ogg",
        pictureFileUrl = pictureFileUrl,
        locale = "ru-ru"
    )
}
