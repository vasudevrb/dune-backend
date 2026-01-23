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
        SET_FACTION_INFLUENCE,
        SET_FEYD_SIGNET_STATUS,
        ADD_OR_REMOVE_COMBAT_UNIT,
        ADD_OR_REMOVE_RESOURCE,
        ADD_OR_REMOVE_VP,
        END_TURN,
        UNLOCK_SWORDMASTER,
        GET_HIGH_COUNCIL,
        UNLOCK_MAKER_HOOK,
        GET_NEXT_CONFLICT,
        ACQUIRE_IMPERIUM_CARD,
        ACQUIRE_RESERVE_CARD,
        PLACE_SPY,
        RECALL_SPY,
        PLACE_CONTROL_FLAG,
        RECALL_CONTROL_FLAG,
        REVEAL,
        GAIN_INTRIGUE_CARD,
        TRASH_INTRIGUE_CARD,
        STEAL_INTRIGUE_CARD,
        DRAW_CARD,
        DISCARD_CARD,
        TRASH_CARD,
        SET_BONUS_SPICE,
        GAIN_OR_LOSE_ALLIANCE,
        GAIN_OR_LOSE_OBJECTIVE,
        BREAK_SHIELD_WALL,
        GET_HAGAL_CARD,
        RESHUFFLE_HAGAL_CARDS,
        ACQUIRE_CONTRACT,
        COMPLETE_CONTRACT,
        CLEAR_ROUND
    }
}
