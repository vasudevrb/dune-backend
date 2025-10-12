package com.vasurb.model

data class Player(
    val id: String,
    val name: String,
    val password: String,
    val character: PlayableCharacter,
    val color: String
) {
    var availableAgents: List<Agent> = listOf()
    var resources: Map<Resource, Int> = mapOf()
    var combatPower: Int = 0
    var victoryPoints: Int = 0
    var factionAlliances: List<Faction> = listOf()
}
