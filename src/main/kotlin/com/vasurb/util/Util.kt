package com.vasurb.util

import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object Util {

    fun getDeterministicRandom(seed: String): Random {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH").withZone(ZoneOffset.UTC)
        val currentHour = formatter.format(Instant.now())

        val combined = "$seed-$currentHour"

        val hashBytes = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        val hashInt = hashBytes.fold(0L) { acc, byte -> (acc shl 8) + (byte.toInt() and 0xff) }

        return Random(hashInt)
    }
}
