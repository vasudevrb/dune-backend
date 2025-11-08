package com.vasurb.api.model.ws_request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.vasurb.api.model.Action

data class WSActionRequest(
    @JsonProperty("action") val type: Action.Type,
    val body: JsonNode?
) {
}
