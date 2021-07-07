package com.epam.brn.validator

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

internal class WithoutPunctuationValidatorTest {

    private val withoutPunctuationValidator = WithoutPunctuationValidator()

    @Test
    fun `should be validate string`() {
        // GIVEN
        val testStringEN = " same text"
        val testStringRU = "тестовый текст"
        val testStringWithNum = "same text with 123"
        val testStringNull = null

        // WHEN
        val resultEN = withoutPunctuationValidator.isValid(testStringEN, null)
        val resultRU = withoutPunctuationValidator.isValid(testStringRU, null)
        val resultNum = withoutPunctuationValidator.isValid(testStringWithNum, null)
        val resultNull = withoutPunctuationValidator.isValid(testStringNull, null)

        // THEN
        resultEN.shouldBeTrue()
        resultRU.shouldBeTrue()
        resultNum.shouldBeTrue()
        resultNull.shouldBeTrue()
    }

    @Test
    fun `should not validate string`() {
        // GIVEN
        val testStringEN = " same text!"
        val testStringRU = " какой-то текст."
        val testStringWithNum = "same, text with 123"

        // WHEN
        val resultEN = withoutPunctuationValidator.isValid(testStringEN, null)
        val resultRU = withoutPunctuationValidator.isValid(testStringRU, null)
        val resultNum = withoutPunctuationValidator.isValid(testStringWithNum, null)

        // THEN
        resultEN.shouldBeFalse()
        resultRU.shouldBeFalse()
        resultNum.shouldBeFalse()
    }
}