package com.epam.brn.enums

enum class ExerciseMechanism {
    WORDS, // show words in random places, play one after another n*2+1 times
    SENTENCES, // show sentences by exercise template, play whole sentence by random words in columns
    MATRIX, // show sentences by exercise template, play sentence words independently by random words in columns
    SIGNALS;
}
