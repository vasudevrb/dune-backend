package com.vasurb.api.model.ws_request

import com.vasurb.model.Card

data class PlaceAgent(
    val agentId: String,
    val locationId: Int
)

data class CardAction(
    val url: String,
    val source: Card.Source
)
