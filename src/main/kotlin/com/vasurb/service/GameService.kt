package com.vasurb.service

import com.vasurb.api.model.Action
import com.vasurb.exception.InvalidActionException
import com.vasurb.model.Board
import com.vasurb.model.Game
import com.vasurb.model.Location
import com.vasurb.model.PlayableCharacter
import com.vasurb.model.Player
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class GameService {

    private val games = ConcurrentHashMap<String, Game>()

    val availableCharacters = PlayableCharacter.entries.toMutableList()
    val presentedCharacters = mutableMapOf<String, List<PlayableCharacter>>()

    init {
        games["123"] = Game()
    }

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

    fun getGame(id: String): Game = games.getOrPut(id, ::Game)
}
