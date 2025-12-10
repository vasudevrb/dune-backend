package com.vasurb.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vasurb.model.Objective.*
import com.vasurb.model.ReserveCard.ReserveType
import kotlin.collections.mapNotNull

data class Game(
    val gameId: String,
    val locations: List<Location> = Location.All().get(),
    val spyLocations: List<SpyLocation> = SpyLocation.All().get(),
    val players: ArrayList<Player> = arrayListOf()
) {

    val bonusSpice: BonusSpice = BonusSpice()
    @JsonIgnore
    val imperiumCards: Deck<ImperiumCard> = Deck(ImperiumCard.All().get())
    @JsonIgnore
    val reserveCards: Map<ReserveType, ArrayList<ReserveCard>> = ReserveCard.All().get()

    @JsonIgnore
    val intrigueCards: Deck<IntrigueCard> = Deck(IntrigueCard.All().get())
    @JsonIgnore
    val conflictCards: Deck<ConflictCard> = Deck(ConflictCard.All().getConflicts(), shuffleAtStart = false)

    @JsonIgnore
    val contracts: Deck<ContractCard> = Deck(ContractCard.All().get())

    var containsRivals = false
    var isStarted = false
    var currentPlayer: String? = null
    var firstPlayer: String? = null
    var shieldWallBroken = false

    var currentConflict: String = conflictCards.draw().url
    var nextConflictLevel: Int = conflictCards.peek()?.conflictType?.level ?: 3

    var currentContracts = contracts.draw(2).map { it.url }.toMutableList() as ArrayList<String>

    val imperiumRow: ArrayList<ImperiumCard> = imperiumCards.draw(5)
    var reserveRow: ArrayList<ReserveCard> = refreshReserveRow()

    @JsonIgnore
    val availableObjectives = arrayListOf(DesertMouse, Ornithopter, Crysknife)
    @JsonIgnore
    val availableCharacters = PlayableCharacter.entries.toMutableList()
    @JsonIgnore
    val presentedCharacters = mutableMapOf<String, List<PlayableCharacter>>()
    @JsonIgnore
    val availableColors: ArrayList<Color> = arrayListOf(Color.GOLD, Color.RED, Color.BLUE, Color.GREEN)
        .shuffled().toMutableList() as ArrayList<Color>

    @JsonIgnore
    val initialTurnOrder: MutableMap<Int, String> = mutableMapOf(
        1 to "",
        2 to "",
        3 to "",
        4 to ""
    )

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
