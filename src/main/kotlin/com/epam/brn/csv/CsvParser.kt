package com.epam.brn.csv

import com.fasterxml.jackson.databind.MappingIterator
import java.io.InputStream

interface CsvParser<Source> {

    fun iterator(file: InputStream): MappingIterator<Source>
}
