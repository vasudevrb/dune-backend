package com.vasurb.api.model.ws_request

import com.vasurb.api.model.Action

data class CardUsed(
    val url: String,
    val playerName: String,
    val type: Action.Type
)

data class RevealCards(
    val urls: List<String>,
    val playerName: String
)
