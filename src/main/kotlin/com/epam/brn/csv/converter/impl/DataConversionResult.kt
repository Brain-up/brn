package com.epam.brn.csv.converter.impl

import java.util.Optional

data class DataConversionResult<Type>(
    val index: Int,
    val line: String,
    val data: Optional<Type>,
    val error: Optional<String>
)
