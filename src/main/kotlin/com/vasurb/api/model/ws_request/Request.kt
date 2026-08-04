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
    val add: Boolean,
    val quantity: Int
)

data class AddOrRemoveResource(
    val resourceType: Resource,
    val add: Boolean,
    val quantity: Int
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

data class SetSignetTrackStatus(
    val status: Int,
)

data class BonusSpiceAction(
    val locationId: Int,
    val add: Boolean
)

data class AcquireSardaukarCommanderAction(
    val commanderId: Int
)

data class AcquireCommanderSkillAction(
    val url: String
)

data class TrashCommanderSkillAction(
    val url: String
)

data class AcquireTechAction(
    val url: String,
    val source: String? //Can be from Kota's Secret Project ability
)

data class FlipTechAction(
    val url: String,
    val flipped: Boolean
)

data class TrashTechAction(
    val url: String,
    val source: String? //Can be from Kota's Secret Project ability
)

data class DeployDuncanAgentAction(
    val agentId: String
)

data class SelectNavigationCardAction(
    val url: String
)

data class RevealNavigationCardAction(
    val url: String,
    val revealed: Boolean
)

data class CommitRaidAction(
    val url: String
)

data class RepeatRaidAction(
    val url: String,
    val repeated: Boolean
)

data class CardAction(
    val url: String,
    val source: Card.SourceDeck
)
