package com.epam.brn.csv.converter

import com.fasterxml.jackson.databind.ObjectReader

@FunctionalInterface
interface ObjectReaderProvider<Type> {
    fun <Type> objectReader(): ObjectReader
}
