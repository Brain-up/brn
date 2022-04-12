package com.epam.brn.dto.azure.tts

// Indicates the speaking rate of the text.
// You can express rate as:
// - A relative value, expressed as a number that acts as a multiplier of the default.
// For example, a value of 1 results in no change in the rate.
// A value of 0.5 results in a halving of the rate. A value of 3 results in a tripling of the rate.
// - A constant value:
enum class AzureRates(val code: String) {
    X_SLOW(" x-slow"),
    SLOW("slow"),
    MEDIUM("medium"),
    FAST("fast"),
    X_FAST("x-fast"),
    DEFAULT("default");
}
