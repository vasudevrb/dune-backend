package com.vasurb.api.model

class Action {

    enum class Type {
        PLACE_AGENT,
        RECALL_AGENT,
        GET_SWORDMASTER,
        PLACE_SPY,
        RECALL_SPY,
        GAIN_RESOURCE,
        SPEND_RESOURCE,
        REVEAL,
        BUY_IMPERIUM_CARD,
        BUY_RESERVE_CARD,
        GAIN_INTRIGUE_CARD,
        USE_INTRIGUE_CARD,
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
