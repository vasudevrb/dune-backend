package com.vasurb.api.model.ws_request

import com.vasurb.model.Card
import com.vasurb.model.Resource

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

data class AddOrRemoveCombatUnit(
    val unitType: String,
    val add: Boolean
)

data class AddOrRemoveResource(
    val resourceType: Resource,
    val add: Boolean
)

data class CardAction(
    val url: String,
    val source: Card.Source
)
