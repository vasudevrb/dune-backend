package com.vasurb.model

import org.springframework.web.socket.WebSocketSession

data class Player(
    val name: String,
    val isHost: Boolean = false,
    var character: PlayableCharacter = PlayableCharacter.MUAD_DIB,
    var color: Color = Color.RED
) {
    var session: WebSocketSession? = null

    var availableAgents: List<Agent> = listOf()
    var resources: Map<Resource, Int> = mapOf()
    var combatPower: Int = 0
    var victoryPoints: Int = 0
    var factionAlliances: List<Faction> = listOf()
}
