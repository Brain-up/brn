package com.epam.brn.config.gson

import com.epam.brn.config.gson.serializer.TypedJsonSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GsonConfiguration(
    private val serializers: List<TypedJsonSerializer<*>>
) {

    @Bean
    fun getGson(): Gson {
        val gsonBuilder = GsonBuilder()
        serializers.forEach {
            gsonBuilder.registerTypeAdapter(it.getType(), it)
        }
        return gsonBuilder.create()
    }
}
