package com.epam.brn.config.gson.serializer

import com.google.gson.JsonSerializer
import java.lang.reflect.Type

interface TypedJsonSerializer<T> : JsonSerializer<T> {

    fun getType(): Type
}
