package com.vasurb.api.model.ws_request

import com.vasurb.model.Card
import com.vasurb.model.Faction
import com.vasurb.model.Objective
import com.vasurb.model.Resource

enum class CombatMovementDestination {
    Combat, Garrison, Supply
}

data class PlaceAgent(
    val agentId: String,
    val locationId: Int
)

data class PlaceSpy(
    val spyId: String,
    val spyLocationId: Int
)

data class PlaceControlFlag(
    val controlFlagId: String,
    val locationId: Int
)

data class RecallAgent(
    val agentId: String
)

data class RecallSpy(
    val spyId: String,
)

data class RecallControlFlag(
    val controlFlagId: String,
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

data class AddOrRemoveVP(
    val add: Boolean
)

data class AcquireImperiumCard(
    val url: String
)

data class AcquireReserveCard(
    val url: String
)

data class AcquireContract(
    val url: String
)

data class CompleteContract(
    val url: String,
    val completed: Boolean
)

data class GainOrLoseAlliance(
    val type: Faction,
    val gained: Boolean
)

data class GainOrLoseObjective(
    val type: Objective,
    val gained: Boolean
)

data class SetFactionInfluence(
    val factionType: Faction,
    val influenceLevel: Int
)

data class SetFeydSignetStatus(
    val status: Int,
)

data class BonusSpiceAction(
    val locationId: Int,
    val add: Boolean
)

data class CardAction(
    val url: String,
    val source: Card.Source
)
