package com.epam.brn.csv.converter.impl

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors
import java.util.stream.Stream
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.stereotype.Service

@Service
class StreamToStringMapper {
    fun getCsvLineNumbersToValues(file: InputStream): Stream<String> {
        val reader = BufferedReader(InputStreamReader(file))

        // collect is necessary to eagerly collect all lines and reset file reader to initial position
        val listOfLinesWithoutHeader = reader
            .lines()
            .skip(NumberUtils.LONG_ONE)
            .collect(Collectors.toList())
        val result = listOfLinesWithoutHeader.stream()

        file.reset()
        return result
    }
}
