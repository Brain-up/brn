package com.epam.brn.service

import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeInRange
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.system.measureTimeMillis

@ExtendWith(MockKExtension::class)
@DisplayName("Test optimization variants")
internal class OptimizationTest {
    @Test
    fun `should use async in foreach correctly Recommended variant`() {
        // GIVEN
        val words = listOf("one", "two", "three", "four")

        // WHEN
        val time = measureTimeMillis {
            runBlocking {
                words
                    .map { word -> async { checkPicture(word) } }
                    .map { it.await() }
            }
        }

        // THEN
        println("Completed in time=$time ms")
        time shouldBeInRange 1000L..1300L
    }

    // @Test // there are some side effects on server side Integration tests run
    fun `should use parallel in foreach correctly Recommended variant`() {
        // GIVEN
        val words = listOf("one", "two", "three", "four")

        // WHEN
        val time = measureTimeMillis {
            words.parallelStream()
                .forEach { word -> checkPictureSleep(word) }
        }

        // THEN
        println("Completed in time=$time ms")
        time shouldBeInRange 1000L..4000L
    }

    @Test
    fun `should use async in foreach correctly in doc example`() {
        runBlocking {
            val time = measureTimeMillis {
                val one = GlobalScope.async { doSomethingUsefulOne() }
                val two = GlobalScope.async { doSomethingUsefulTwo() }
                println("The answer is ${one.await() + two.await()}")
            }
            println("Completed in $time ms")
        }
    }

    @Test
    fun `test flow in foreach`() {
        // GIVEN
        val words = listOf("one", "two", "three", "four")

        // WHEN
        val time = measureTimeMillis {
            runBlocking {
                words
                    .asFlow()
                    .map { word -> checkPicture(word) }
                    .collect()
            }
        }
        // THEN
        println("Completed in time=$time ms")
        time shouldBeGreaterThan 3000L
    }

    @Test
    fun `test flow example in foreach`() {
        // GIVEN
        val words = listOf("one", "two", "three", "four")

        // WHEN
        val time = measureTimeMillis {
            runBlocking {
                // Launch a concurrent coroutine to check if the main thread is blocked
                launch {
                    words.forEach {
                        println("I'm not blocked $it")
                    }
                }
                // Collect the flow
                simple().collect { value -> println(value) }
            }
        }
        // THEN
        println("Completed in time=$time ms")
        time shouldBeGreaterThan 3000L
    }

    @Test
    fun `test flow example 2`() {
        // WHEN
        val time = measureTimeMillis {
            runBlocking {
                (1..3).asFlow() // a flow of requests
                    .map { request -> performRequest(request) }
                    .collect { response -> println(response) }
            }
        }
        // THEN
        println("Completed in time=$time ms")
        time shouldBeGreaterThan 3000L
    }
}

suspend fun checkPicture(name: String): Boolean {
    delay(1000L)
    println("process $name")
    return true
}

fun checkPictureSleep(name: String): Boolean {
    Thread.sleep(1000L)
    println("process $name")
    return true
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}

fun simple(): Flow<Int> = flow { // flow builder
    for (i in 1..3) {
        delay(1000) // pretend we are doing something useful here
        emit(i) // emit next value
    }
}

suspend fun performRequest(request: Int): String {
    delay(1000) // imitate long-running asynchronous work
    return "response $request"
}
