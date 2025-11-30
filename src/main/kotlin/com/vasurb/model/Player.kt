package com.vasurb.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class Player(
    val name: String,
    val isHost: Boolean = false,
    val color: Color
) {
    @get:JsonProperty
    var character: CharacterModel? = null

    @get:JsonProperty
    val agents = arrayListOf(
        Agent("agent-${name}#1"),
        Agent("agent-${name}#2"),
        Agent("agent-${name}#3"),
    )

    @get:JsonProperty
    val spies = arrayListOf(
        Spy("spy-${name}#1"),
        Spy("spy-${name}#2"),
        Spy("spy-${name}#3"),
    )

    @get:JsonProperty
    val controlFlags = arrayListOf(
        ControlFlag("control_flag-${name}#1"),
        ControlFlag("control_flag-${name}#2"),
        ControlFlag("control_flag-${name}#3"),
    )

    @get:JsonProperty
    var victoryPoints: Int = 0

    @get:JsonProperty
    var objectives: ArrayList<Objective> = arrayListOf()
    var numCards: NumCardsModel = NumCardsModel()
    var resources: MutableMap<Resource, Int> = mutableMapOf(
        Resource.water to 1,
        Resource.spice to 0,
        Resource.solari to 0,
    )
    var factionInfluences: MutableMap<Faction, Int> = mutableMapOf(
        Faction.Fremen to 0,
        Faction.BeneGesserit to 0,
        Faction.SpacingGuild to 0,
        Faction.Emperor to 0,
    )

    var swordmasterUnlocked: Boolean = false
    var makerHookUnlocked: Boolean = false
    var combat: CombatModel = CombatModel()

    val private: Private = Private()

    @JsonIgnore
    var readyState: String? = null

    data class Private(
        val discardedCards: ArrayList<AgentCard> = arrayListOf(),
        @JsonIgnore
        val drawPile: Deck<AgentCard> = Deck(StarterCard.getAll(), discardedCards),
        val inHandCards: ArrayList<AgentCard> = drawPile.draw(5),
        val inPlayCards: ArrayList<AgentCard> = arrayListOf(),
        val intrigueCards: ArrayList<IntrigueCard> = arrayListOf(),
        val usedIntrigues: ArrayList<IntrigueCard> = arrayListOf()
    )
}
