package com.vasurb.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object Util {

    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun getDeterministicRandom(seed: String): Random {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH").withZone(ZoneOffset.UTC)
        val currentHour = formatter.format(Instant.now())

        val combined = "$seed-$currentHour"

        val hashBytes = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        val hashInt = hashBytes.fold(0L) { acc, byte -> (acc shl 8) + (byte.toInt() and 0xff) }

        return Random(hashInt)
    }

    inline fun <reified T> ObjectMapper.getAs(node: JsonNode?): T {
        return convertValue(
            node,
            object : TypeReference<T>() {}
        )
    }

    inline fun <reified T> ObjectMapper.toTree(obj: Any?, includePrivate: Boolean = false): T {
        val tree = convertValue(
            obj,
            object : TypeReference<T>() {}
        )

        if (!includePrivate && tree is ObjectNode) {
            tree.remove("private")
            if (tree.has("players") && tree["players"] is ArrayNode) {
                val playersNode = tree["players"] as ArrayNode
                for (playerNode in playersNode) {
                    if (playerNode is ObjectNode) {
                        playerNode.remove("private")
                    }
                }
            }
        }

        return tree
    }

    fun SimpMessagingTemplate.convertAndSendToUser(user: String, destination: String, payload: Any?) {
        if (payload == null) {
            println("Given payload is null. Not sending anything")
            return
        }

        convertAndSendToUser(
            user,
            destination,
            payload
        )
    }

    fun SimpMessagingTemplate.convertAndSend(destination: String, payload: Any?) {
        if (payload == null) {
            println("Given payload is null. Not sending anything")
            return
        }

        convertAndSend(destination, payload)
    }

    fun <T> ArrayList<T>.getRandomAndRemove(): T {
        val polled = random()
        remove(polled)
        return polled
    }

    fun <K> MutableMap<K, Int>.inc(key: K): Int = merge(key, 1, Math::addExact)!!
    fun <K> MutableMap<K, Int>.dec(key: K): Int = merge(key, -1, Math::addExact)!!
}
