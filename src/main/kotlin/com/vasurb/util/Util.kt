package com.vasurb.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.vasurb.model.Game
import com.vasurb.model.Player
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object Util {

    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

        // Add extra fields ONLY when T is a specific type
        if (obj is Player && tree is ObjectNode) {
            addCardStats(obj, tree)
        } else if (obj is Game && tree is ObjectNode) {
            val playersNode = tree["players"] as ArrayNode
            for (playerNode in playersNode) {
                val player = obj.players.find { it.name == playerNode.get("name").asText() }
                player?.let {addCardStats(it, playerNode as ObjectNode)}
            }
        }

        return tree
    }

    fun addCardStats(player: Player, tree: ObjectNode) {
        val numCards = tree.objectNode().apply {
            put("inHand", player.private.inHandCards.size)
            put("inPlay", player.private.inPlayCards.size)
            put("inDiscardPile", player.private.discardedCards.size)
            put("inDrawPile", player.private.drawPile.size())
            put("intrigues", player.private.intrigueCards.size)
            put("openContracts", player.contracts.filter { !it.completed }.size)
            put("completedContracts", player.contracts.filter { it.completed }.size)
        }

        tree.set<ObjectNode>("numCards", numCards)
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

  fun <T> List<T>.filterIf(condition: Boolean, predicate: (T) -> Boolean): List<T> =
    if (condition) filter(predicate) else this


    fun <K> MutableMap<K, Int>.inc(key: K, quantity: Int = 1): Int = merge(key, quantity, Math::addExact)!!
    fun <K> MutableMap<K, Int>.dec(key: K, quantity: Int = 1): Int = merge(key, quantity * -1, Math::addExact)!!
}
