package com.vasurb.api.model.ws_request

import com.vasurb.model.Card

enum class CombatMovementDestination {
    Combat, Garrison, Supply
}

data class PlaceAgent(
    val agentId: String,
    val locationId: Int
)

data class RecallAgent(
    val agentId: String
)

data class MoveCombatUnit(
    val unitType: String,
    val destination: CombatMovementDestination
)

data class CardAction(
    val url: String,
    val source: Card.Source
)
