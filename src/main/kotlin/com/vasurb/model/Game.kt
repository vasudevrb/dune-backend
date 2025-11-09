package com.vasurb.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vasurb.model.Objective.*

data class Game(
    val gameId: String,
    val locations: List<Location> = listOf(
        Location("Deep Desert", 1)
    ),
    val players: ArrayList<Player> = arrayListOf()
) {

    var isStarted = false
    var currentPlayer: String? = null
    var firstPlayer: String? = null

    @JsonIgnore
    val imperiumCards: Deck<ImperiumCard> = Deck(arrayListOf())
    @JsonIgnore
    val intrigueCards: Deck<IntrigueCard> = Deck(arrayListOf())
    @JsonIgnore
    val conflictCards: Deck<ConflictCard> = Deck(ConflictCard.getConflicts())

    val imperiumRow: ArrayList<ImperiumCard> = imperiumCards.draw(5)

    @JsonIgnore
    val availableObjectives = arrayListOf(DesertMouse, Ornithopter, Crysknife)
    @JsonIgnore
    val availableCharacters = PlayableCharacter.entries.toMutableList()
    @JsonIgnore
    val presentedCharacters = mutableMapOf<String, List<PlayableCharacter>>()
    @JsonIgnore
    val availableColors = mutableListOf(Color.RED, Color.BLUE, Color.GREEN)

    fun getPlayerByName(playerName: String): Player? = players.firstOrNull { it.name == playerName }

    fun addOrUpdatePlayer(playerName: String, player: Player) {
        val index = players.indexOfFirst { it.name == playerName }
        if (index == -1) players.add(player)
        else players[index] = player
    }
}
