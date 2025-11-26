package com.vasurb.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vasurb.model.Objective.*
import com.vasurb.model.ReserveCard.ReserveType

data class Game(
    val gameId: String,
    val locations: List<Location> = Location.LOCATIONS,
    val spyLocations: List<SpyLocation> = SpyLocation.SPY_LOCATIONS,
    val players: ArrayList<Player> = arrayListOf()
) {

    var isStarted = false
    var currentPlayer: String? = null
    var firstPlayer: String? = null

    @JsonIgnore
    val imperiumCards: Deck<ImperiumCard> = Deck(ImperiumCard.getAll())
    @JsonIgnore
    val reserveCards: Map<ReserveType, ArrayList<ReserveCard>> = ReserveCard.getAll()

    @JsonIgnore
    val intrigueCards: Deck<IntrigueCard> = Deck(arrayListOf())
    @JsonIgnore
    val conflictCards: Deck<ConflictCard> = Deck(ConflictCard.getConflicts())

    val imperiumRow: ArrayList<ImperiumCard> = imperiumCards.draw(5)
    val reserveRow: ArrayList<ReserveCard> = ReserveType.entries
        .mapNotNull { reserveCards.getValue(it).removeFirstOrNull() }
        .toMutableList() as ArrayList<ReserveCard>

    @JsonIgnore
    val availableObjectives = arrayListOf(DesertMouse, Ornithopter, Crysknife)
    @JsonIgnore
    val availableCharacters = PlayableCharacter.entries.toMutableList()
    @JsonIgnore
    val presentedCharacters = mutableMapOf<String, List<PlayableCharacter>>()
    @JsonIgnore
    val availableColors: ArrayList<Color> = arrayListOf(Color.RED, Color.BLUE, Color.GREEN)
        .shuffled().toMutableList() as ArrayList<Color>

    fun getPlayerByName(playerName: String): Player? = players.firstOrNull { it.name == playerName }

    fun addOrUpdatePlayer(playerName: String, player: Player) {
        val index = players.indexOfFirst { it.name == playerName }
        if (index == -1) players.add(player)
        else players[index] = player
    }
}
