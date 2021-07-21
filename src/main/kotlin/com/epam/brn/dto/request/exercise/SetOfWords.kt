package com.epam.brn.dto.request.exercise

data class SetOfWords(
    val count: List<String>?,
    val objectDescription: List<String>?,
    val objectWord: List<String>?,
    val objectAction: List<String>?,
    val additionObjectDescription: List<String>?,
    val additionObject: List<String>?
)
