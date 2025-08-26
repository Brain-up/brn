package com.epam.brn.dto.azure.tts

// Indicates the baseline pitch for the text.
// You can express the pitch as:
// - An absolute value, expressed as a number followed by "Hz" (Hertz).
// For example, <prosody pitch="600Hz">some text</prosody>.
// A relative value, expressed as a number preceded by "+" or "-"
// and followed by "Hz" or "st" that specifies an amount to change the pitch.
// For example: <prosody pitch="+80Hz">some text</prosody> or <prosody pitch="-2st">some text</prosody>.
// The "st" indicates the change unit is semitone, which is half of a tone (a half step) on the standard diatonic scale.
// - A constant value:
enum class AzurePitches(
    val code: String,
) {
    X_LOW("x-low"),
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    X_HIGH("x-high"),
    DEFAULT("default"),
}
