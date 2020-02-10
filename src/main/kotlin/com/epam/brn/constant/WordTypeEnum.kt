package com.epam.brn.constant

enum class WordTypeEnum {
    OBJECT,
    OBJECT_ACTION,
    OBJECT_DESCRIPTION,
    ADDITION_OBJECT,
    ADDITION_OBJECT_DESCRIPTION,
    COUNT,
    SENTENCE
}

val mapPositionToWordType = mapOf(
    0 to WordTypeEnum.COUNT,
    1 to WordTypeEnum.OBJECT_DESCRIPTION,
    2 to WordTypeEnum.OBJECT,
    3 to WordTypeEnum.OBJECT_ACTION,
    4 to WordTypeEnum.ADDITION_OBJECT_DESCRIPTION,
    5 to WordTypeEnum.ADDITION_OBJECT
)
