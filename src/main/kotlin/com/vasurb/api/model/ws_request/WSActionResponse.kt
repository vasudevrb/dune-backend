package com.vasurb.api.model.ws_request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class WSActionResponse(
    @JsonProperty("action") val type: Type,
    val body: JsonNode?
) {

    enum class Type {
        START_GAME,
        GET_CHARACTER_READY_STATES,
        UPDATE_PLAYER,
        UPDATE_LOCATION,
        CARD_USED,
    }

    data class CardUsed(val url: String, val playerName: String)
}
