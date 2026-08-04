package com.vasurb.model

import com.fasterxml.jackson.annotation.JsonIgnore

data class Player(
    val name: String,
    val isHost: Boolean = false,
    val isRival: Boolean = false
) {
    var character: CharacterModel? = null
    var color: Color = Color.GOLD
    var victoryPoints: Int = 0

    val agents = arrayListOf(
        Agent("agent-${name}#1"),
        Agent("agent-${name}#2"),
        Agent("agent-${name}#3"),
    )

    val spies = arrayListOf(
        Spy("spy-${name}#1"),
        Spy("spy-${name}#2"),
        Spy("spy-${name}#3"),
    )

    val controlFlags = arrayListOf(
        ControlFlag("control_flag-${name}#1"),
        ControlFlag("control_flag-${name}#2"),
        ControlFlag("control_flag-${name}#3"),
    )

    val contracts = arrayListOf<Contract>()

    var objectives: ArrayList<Objective> = arrayListOf()
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
    var factionAlliances: ArrayList<Faction> = arrayListOf()

    var raids: ArrayList<Raid> = arrayListOf()

    var swordmasterUnlocked: Boolean = false
    var hasAtomicsToken: Boolean = true
    var makerHookUnlocked: Boolean = false
    var combat: CombatModel = CombatModel()

    val skills = arrayListOf<SardaukarSkill>()
    val techs = arrayListOf<TechTile>()

    val private: Private = Private()

    @JsonIgnore
    var readyState: String? = null

    data class Private(
        val discardedCards: ArrayList<AgentCard> = arrayListOf(),
        @JsonIgnore
        val drawPile: Deck<AgentCard> = Deck(StarterCard.getAll(), discardedCards),
        val inHandCards: ArrayList<AgentCard> = arrayListOf(),
        val inPlayCards: ArrayList<AgentCard> = arrayListOf(),
        val intrigueCards: ArrayList<IntrigueCard> = arrayListOf(),
        val usedIntrigues: ArrayList<IntrigueCard> = arrayListOf()
    )
}
