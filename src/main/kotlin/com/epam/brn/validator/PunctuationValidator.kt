package com.epam.brn.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.text.Regex

class PunctuationValidator : ConstraintValidator<WithoutPunctuation, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value != null) {
            return !value.contains(regex)
        }
        return true
    }

    companion object {
        private val regex = Regex("[^А-яA-z0-9ё ]")
    }
}
