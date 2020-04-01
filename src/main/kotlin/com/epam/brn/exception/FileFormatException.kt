package com.epam.brn.exception

import java.lang.RuntimeException

class FileFormatException : RuntimeException("Formatting error. Please upload file with csv extension.")
