package com.epam.brn.validator

import javax.validation.Constraint
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PunctuationValidator::class])
@Target(allowedTargets = [FIELD ])
@Retention(AnnotationRetention.RUNTIME)
annotation class WithoutPunctuation(
    val message: String = "Must not contain punctuation marks",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)
