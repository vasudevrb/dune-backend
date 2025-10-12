package com.vasurb.model

data class Player(
    val id: String,
    val name: String,
    val password: String,
    val character: PlayableCharacter,
    val color: String,
    val availableAgents: List<Agent>,
    val resources: Map<Resource, Int>,
    val combatPower: Int
)
