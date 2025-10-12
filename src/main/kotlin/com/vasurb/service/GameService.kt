package com.vasurb.service

import com.vasurb.api.model.Action
import com.vasurb.exception.InvalidActionException
import com.vasurb.model.Board
import com.vasurb.model.Location
import com.vasurb.model.Player
import org.springframework.stereotype.Component

@Component
class GameService {

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
}
