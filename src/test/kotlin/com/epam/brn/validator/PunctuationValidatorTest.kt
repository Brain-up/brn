package com.epam.brn.validator

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

internal class PunctuationValidatorTest {
    private val punctuationValidator = PunctuationValidator()

    @Test
    fun `should be validate string`() {
        // GIVEN
        val testStringEN = " same text"
        val testStringRU = "тестовый текст"
        val testStringRU2 = "Мамочка идёт"
        val testStringWithNum = "same text with 123"
        val testStringNull = null

        // WHEN
        val resultEN = punctuationValidator.isValid(testStringEN, null)
        val resultRU = punctuationValidator.isValid(testStringRU, null)
        val resultRU2 = punctuationValidator.isValid(testStringRU2, null)
        val resultNum = punctuationValidator.isValid(testStringWithNum, null)
        val resultNull = punctuationValidator.isValid(testStringNull, null)

        // THEN
        resultEN.shouldBeTrue()
        resultRU.shouldBeTrue()
        resultRU2.shouldBeTrue()
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
        val resultEN = punctuationValidator.isValid(testStringEN, null)
        val resultRU = punctuationValidator.isValid(testStringRU, null)
        val resultNum = punctuationValidator.isValid(testStringWithNum, null)

        // THEN
        resultEN.shouldBeFalse()
        resultRU.shouldBeFalse()
        resultNum.shouldBeFalse()
    }
}
