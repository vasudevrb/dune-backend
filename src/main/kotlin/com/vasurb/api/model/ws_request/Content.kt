package com.vasurb.api.model.ws_request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class WSActionResponse(val messages: List<Message>) {

    sealed interface Recipient
    data class SinglePlayer(val playerName: String): Recipient
    object AllPlayers: Recipient

    data class Message(val recipient: Recipient, val content: Content?)
    data class Content(@JsonProperty("action") val type: Type, val body: JsonNode?)

    enum class Type {
        START_GAME,
        GET_CHARACTER_READY_STATES,
        UPDATE_PLAYER,
        UPDATE_LOCATION,
        CARD_USED,
    }

}
