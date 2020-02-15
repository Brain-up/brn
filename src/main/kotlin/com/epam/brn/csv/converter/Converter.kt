package com.epam.brn.csv.converter

interface Converter<Source, Target> {

    fun convert(source: Source): Target
}
