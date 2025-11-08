package com.vasurb.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.socket.WebSocketSession

data class Player(
    val name: String,
    val isHost: Boolean = false,
    val color: Color
) {
    @get:JsonProperty
    var character: CharacterModel = CharacterModel.get(PlayableCharacter.MUAD_DIB)

    @get:JsonProperty
    val agents = arrayListOf(
    Agent("agent-${name}#1"),
    Agent("agent-${name}#2"),
    Agent("agent-${name}#3"),
    )
    @get:JsonProperty
    val spies = arrayListOf(
    Spy("spy-${name}#1"),
    Spy("spy-${name}#1"),
    Spy("spy-${name}#1"),
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
    var objectives: List<Objective> = listOf()
    var numCards: NumCardsModel = NumCardsModel()
    var resources: Map<Resource, Int> = mapOf()
    var factionInfluences: Map<Faction, Int> = mapOf()

    var swordmasterUnlocked: Boolean = false
    var makerHookUnlocked: Boolean = false
    var combat: CombatModel = CombatModel()

    @JsonIgnore
    var readyState  : String? = null
}
