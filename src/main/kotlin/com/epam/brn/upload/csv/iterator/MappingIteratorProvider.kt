package com.epam.brn.upload.csv.iterator

import com.fasterxml.jackson.databind.MappingIterator
import java.io.InputStream

interface MappingIteratorProvider<ObjectType> {

    fun iterator(file: InputStream): MappingIterator<ObjectType>
}
