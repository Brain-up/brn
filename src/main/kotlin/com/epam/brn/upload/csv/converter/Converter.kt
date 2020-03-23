package com.epam.brn.upload.csv.converter

interface Converter<Source, Target> {

    fun convert(source: Source): Target
}
