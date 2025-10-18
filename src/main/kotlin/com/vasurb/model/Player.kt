package com.vasurb.model

data class Player(
    val name: String,
    val isHost: Boolean = false,
    var character: PlayableCharacter = PlayableCharacter.MUAD_DIB,
    var color: Color = Color.RED
) {
    var availableAgents: List<Agent> = listOf()
    var resources: Map<Resource, Int> = mapOf()
    var combatPower: Int = 0
    var victoryPoints: Int = 0
    var factionAlliances: List<Faction> = listOf()
}
