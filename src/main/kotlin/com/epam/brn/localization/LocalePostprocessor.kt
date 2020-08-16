package com.epam.brn.localization

interface LocalePostprocessor<Dto> {
    fun postprocess(dto: Dto): Dto
}
