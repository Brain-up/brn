package com.epam.brn.csv.converter

import com.fasterxml.jackson.databind.ObjectReader

interface ObjectReaderProvider<Csv> {
    fun objectReader(): ObjectReader
}
