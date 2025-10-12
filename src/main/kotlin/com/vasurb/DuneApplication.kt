package com.vasurb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DuneApplication

fun main(args: Array<String>) {
    runApplication<DuneApplication>(*args)
}
