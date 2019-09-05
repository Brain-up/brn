package com.lifescience.brn.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppConfig {
    @Value("\${pr1}")
    lateinit var pr1: String
}