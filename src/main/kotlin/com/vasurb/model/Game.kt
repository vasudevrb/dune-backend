package com.vasurb.model

data class Game(
    val gameId: String,
    val locations: List<Location> = listOf(
        Location("Deep Desert", 1)
    ),
    val players: ArrayList<Player> = arrayListOf()
) {

    var currentPlayer: String? = null
    var firstPlayer: String? = null

    val availableCharacters = PlayableCharacter.entries.toMutableList()
    val presentedCharacters = mutableMapOf<String, List<PlayableCharacter>>()
    val availableColors = mutableListOf(Color.RED, Color.BLUE, Color.GREEN)

    fun getPlayerByName(playerName: String): Player? = players.firstOrNull { it.name == playerName }

    fun addOrUpdatePlayer(playerName: String, player: Player) {
        val index = players.indexOfFirst { it.name == playerName }
        if (index == -1) players.add(player)
        else players[index] = player
    }
}
