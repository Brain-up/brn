package com.epam.brn.service

interface LocalePostprocessor<Dto> {
    fun postprocess(dto: Dto): Dto
}
