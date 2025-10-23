package com.vasurb.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class ActionResponse(
    @JsonProperty("action") val type: Action.Type,
    val body: JsonNode?
) {
}
