package com.vasurb.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vasurb.model.Objective.*
import com.vasurb.model.ReserveCard.ReserveType
import kotlin.collections.mapNotNull

data class Game(
    val gameId: String,
    val locations: List<Location> = Location.LOCATIONS.toMutableList(),
    val spyLocations: List<SpyLocation> = SpyLocation.SPY_LOCATIONS.toMutableList(),
    val players: ArrayList<Player> = arrayListOf()
) {

    @JsonIgnore
    val imperiumCards: Deck<ImperiumCard> = Deck(ImperiumCard.getAll())
    @JsonIgnore
    val reserveCards: Map<ReserveType, ArrayList<ReserveCard>> = ReserveCard.getAll()

    @JsonIgnore
    val intrigueCards: Deck<IntrigueCard> = Deck(IntrigueCard.getAll())
    @JsonIgnore
    val conflictCards: Deck<ConflictCard> = Deck(ConflictCard.getConflicts(), shuffleAtStart = false)

    var isStarted = false
    var currentPlayer: String? = null
    var firstPlayer: String? = null

    var currentConflict: String = conflictCards.draw().url
    var nextConflictLevel: Int = conflictCards.peek()?.conflictType?.level ?: 3

    val imperiumRow: ArrayList<ImperiumCard> = imperiumCards.draw(5)
    var reserveRow: ArrayList<ReserveCard> = refreshReserveRow()

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

    fun refreshReserveRow(): ArrayList<ReserveCard> {
        return ReserveType.entries
        .mapNotNull { reserveCards.getValue(it).firstOrNull() }
            .toMutableList() as ArrayList<ReserveCard>
    }
}
