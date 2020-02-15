package com.epam.brn.csv

import com.fasterxml.jackson.databind.MappingIterator
import java.io.InputStream

interface CsvParser<Source> {

    fun parseCsvFile(file: InputStream): MappingIterator<Source>
}
