package com.epam.brn.dto

data class TaskDto(
    val id: String,
    val word: String,
    val order: Int,
    val audioFileId: String,
    val words: List<String>,
    val exerciseId: String
)