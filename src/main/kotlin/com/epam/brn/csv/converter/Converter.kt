package com.epam.brn.csv.converter

import com.fasterxml.jackson.databind.MappingIterator
import java.io.InputStream

interface Converter<Source, Target> {
    fun iteratorProvider(): (file: InputStream) -> MappingIterator<Source>
    fun convert(source: Source): Target
}
