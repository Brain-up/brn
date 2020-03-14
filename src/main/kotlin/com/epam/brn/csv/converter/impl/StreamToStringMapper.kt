package com.epam.brn.csv.converter.impl

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.stereotype.Service

@Service
class StreamToStringMapper {
    fun getCsvLineNumbersToValues(file: InputStream): Map<Int, String> {
        val reader = BufferedReader(InputStreamReader(file))

        val result = mutableMapOf<Int, String>()
        val listOfLinesWithoutHeader = reader
            .lines()
            .skip(NumberUtils.LONG_ONE)
            .collect(Collectors.toList())
        listOfLinesWithoutHeader.forEachIndexed { index, s ->
            result[index + 2] = s
        }

        file.reset()
        return result
    }
}
