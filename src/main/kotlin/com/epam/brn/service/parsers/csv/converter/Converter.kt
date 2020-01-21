package com.epam.brn.service.parsers.csv.converter

interface Converter<Source, Target> {

    fun convert(source: Source): Target
}
