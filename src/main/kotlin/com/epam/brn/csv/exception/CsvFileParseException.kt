package com.epam.brn.csv.exception

class CsvFileParseException(val errors: List<String>) :
    RuntimeException("Parsing error. Please check csv file content format.")
