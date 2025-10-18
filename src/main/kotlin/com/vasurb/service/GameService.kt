package com.vasurb.service

import com.vasurb.api.model.Action
import com.vasurb.exception.ExpiredGameException
import com.vasurb.exception.InvalidActionException
import com.vasurb.model.Board
import com.vasurb.model.Game
import com.vasurb.model.Location
import com.vasurb.model.PlayableCharacter
import com.vasurb.model.Player
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class GameService {

    private val games = ConcurrentHashMap<String, Game>()

    val availableCharacters = PlayableCharacter.entries.toMutableList()
    val presentedCharacters = mutableMapOf<String, List<PlayableCharacter>>()

    fun takeAction(
        player: Player,
        board: Board,
        action: Action
    ) {

    }

    fun sendAgent(
        player: Player,
        location: Location
    ) {
        if (player.availableAgents.isEmpty()) {
            throw InvalidActionException("No available agents!")
        }

        location.agents.add(player.availableAgents.first())
    }

    fun createGame(player: Player): Game {
        val gameId = UUID.randomUUID().toString()
        val game = Game(gameId)
        games[gameId] = game
        game.addOrUpdatePlayer(player.name, player)
        return game
    }

    fun addPlayer(player: Player, gameId: String): Game {
        val game = getGame(gameId)
        if (game.getPlayerByName(player.name) == null) {
            game.addOrUpdatePlayer(player.name, player)
        } else {
            throw RuntimeException("${player.name} is already assigned to this game")
        }
        return game
    }

    fun updatePlayer(player: Player, gameId: String): Game {
        val game = getGame(gameId)
        game.addOrUpdatePlayer(player.name, player)
        return game
    }

    fun getPlayer(gameId: String, playerName: String): Player {
        val game = getGame(gameId)
        return game.getPlayerByName(playerName) ?: throw ExpiredGameException("Player $playerName is not assigned to this game")
    }

    fun getGame(id: String): Game = games[id] ?: throw ExpiredGameException("Game not found. Create a game before retrieving it.")
}
