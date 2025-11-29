package com.vasurb.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

class Action(
    @JsonProperty("action")
    val type: Type,
    val body: JsonNode?
) {

    enum class Type {
        ADD_TO_GAME,
        GET_CHARACTER_READY_STATES,
        START_GAME,
        RESUME_GAME,
        USE_CARD,
        PLACE_AGENT,
        RECALL_AGENT,
        MOVE_COMBAT_UNIT,
        ADD_OR_REMOVE_COMBAT_UNIT,
        ADD_OR_REMOVE_RESOURCE,
        END_TURN,
        GET_SWORDMASTER,
        PLACE_SPY,
        RECALL_SPY,
        GAIN_RESOURCE,
        SPEND_RESOURCE,
        REVEAL,
        BUY_IMPERIUM_CARD,
        BUY_RESERVE_CARD,
        GAIN_INTRIGUE_CARD,
        STEAL_INTRIGUE_CARD,
        DRAW_CARD,
        DISCARD_CARD,
        TRASH_CARD,
        SEND_TROOP_TO_GARRISON,
        SEND_TROOP_TO_COMBAT,
        SEND_TROOP_TO_SUPPLY,
        SEND_WORM_TO_COMBAT,
        SEND_WORM_TO_SUPPLY,
        INCREASE_COMBAT_POWER,
        INCREASE_FACTION_INFLUENCE,
        DECREASE_FACTION_INFLUENCE,
        CLEAR_ROUND
    }
}
