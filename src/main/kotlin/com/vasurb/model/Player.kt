package com.vasurb.model

data class Player(
    val id: String
) {
    var name: String = ""
    var character: PlayableCharacter = PlayableCharacter.MUAD_DIB
    var color: String = "gold"

    var availableAgents: List<Agent> = listOf()
    var resources: Map<Resource, Int> = mapOf()
    var combatPower: Int = 0
    var victoryPoints: Int = 0
    var factionAlliances: List<Faction> = listOf()
}
