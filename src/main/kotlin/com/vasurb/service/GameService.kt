package com.vasurb.service

import com.vasurb.api.model.Action.Type.DISCARD_CARD
import com.vasurb.api.model.Action.Type.TRASH_CARD
import com.vasurb.api.model.Action.Type.USE_CARD
import com.vasurb.api.model.ws_request.AddToGame
import com.vasurb.api.model.ws_request.CardAction
import com.vasurb.api.model.ws_request.CardUsed
import com.vasurb.api.model.ws_request.PlaceAgent
import com.vasurb.api.model.ws_request.WSActionRequest
import com.vasurb.api.model.ws_request.WSActionResponse
import com.vasurb.exception.ExpiredGameException
import com.vasurb.model.AgentCard
import com.vasurb.model.Card
import com.vasurb.model.Game
import com.vasurb.model.IntrigueCard
import com.vasurb.model.Location
import com.vasurb.model.Objective.Crysknife
import com.vasurb.model.Objective.DesertMouse
import com.vasurb.model.Objective.Ornithopter
import com.vasurb.model.Player
import com.vasurb.util.Util.getAs
import com.vasurb.util.Util.getRandomAndRemove
import com.vasurb.util.Util.mapper
import com.vasurb.util.Util.toTree
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class GameService {

    private val games = ConcurrentHashMap<String, Game>()

    fun addToGame(
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<AddToGame>(action.body)
        val game = getGame(gameId)
        val player = getPlayer(gameId, body.playerName)
        assignObjective(game, player)

        player.readyState = "READY"
        return getPlayerReadyStates(gameId)
    }

    private fun assignObjective(game: Game, player: Player) {
        if (game.players.size <= 3) {
            player.objectives.add(game.availableObjectives.getRandomAndRemove())
        } else {
            val FourPObjectives = arrayListOf(DesertMouse, Ornithopter)
            player.objectives.add(FourPObjectives.getRandomAndRemove())

            val crysKnifePlayer = game.players
                .find { it.objectives.contains(Crysknife) }
            crysKnifePlayer?.objectives = arrayListOf(FourPObjectives[0])
        }
    }

    fun getPlayerReadyStates(gameId: String): WSActionResponse {
        val players = getGame(gameId).players

        val responseBody = mapper.createArrayNode().apply {
            players.forEach {
                add(mapper.createObjectNode().apply {
                    put("playerName", it.name)
                    put("color", it.color.name)
                    put("characterName", it.character?.name)
                    putArray("characterUrls").apply {
                        it.character?.let{ character -> character.urls.forEach { url -> add(url) }}
                    }
                    put("avatarUrl", it.character?.avatarUrl)
                    put("objective", it.objectives[0].name)
                    put("status", if (it.readyState != null) "Ready" else "Not ready")
                })
            }
        }

        val response = WSActionResponse.Content(
            WSActionResponse.Type.GET_CHARACTER_READY_STATES,
            responseBody
        )

        return WSActionResponse(
            listOf(
                WSActionResponse.Message(WSActionResponse.AllPlayers, response)
            )
        )
    }

    fun getStartGame(gameId: String): WSActionResponse {
        val game = getGame(gameId)
        game.isStarted = true

        val desertMousePlayers = game.players.filter { it.objectives.contains(DesertMouse) }
        val firstPlayer = desertMousePlayers.random()
        game.players.remove(firstPlayer)
        game.players.add(0, firstPlayer)

        game.firstPlayer = firstPlayer.name
        game.currentPlayer = firstPlayer.name

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(WSActionResponse.Message(
            WSActionResponse.AllPlayers,
            WSActionResponse.Content(WSActionResponse.Type.START_GAME, mapper.toTree(game))
        ))

        game.players.forEach {
            val response = WSActionResponse.Content(
                WSActionResponse.Type.UPDATE_PLAYER,
                mapper.toTree(it, includePrivate = true)
            )
            messages.add(WSActionResponse.Message(
                WSActionResponse.SinglePlayer(it.name), response
            ))
        }

        return WSActionResponse(messages)
    }

    fun getResumeGame(gameId: String, playerName: String): WSActionResponse {
        val game = getGame(gameId)
        return WSActionResponse(listOf(WSActionResponse.Message(
            WSActionResponse.SinglePlayer(playerName),
            WSActionResponse.Content(WSActionResponse.Type.START_GAME, mapper.toTree(game, includePrivate = true))
        )))
    }

    fun handleCardAction(
        gameId: String,
        playerName: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<CardAction>(action.body)
        val game = getGame(gameId)
        val player = game.players.find { it.name == playerName }
            ?: return WSActionResponse(listOf())
        val sourceList = when(body.source) {
            Card.Source.HAND -> player.private.inHandCards
            Card.Source.PLAY -> player.private.inPlayCards
            Card.Source.DISCARD -> player.private.discardedCards
            Card.Source.INTRIGUE -> player.private.intrigueCards
        }

        val removable = sourceList.find { it.url == body.url }
        val messages = arrayListOf<WSActionResponse.Message>()

        removable?.let {
            sourceList.remove(it)
            when (action.type) {
                USE_CARD -> useCard(it, body, player)
                DISCARD_CARD -> discardCard(it as AgentCard, body, player)
                TRASH_CARD -> {}
                else -> {}
            }

            messages.add(WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            ))

            messages.add(WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.CARD_USED, mapper.toTree(CardUsed(body.url, playerName)))
            ))
        }

        return WSActionResponse(messages)
    }

    private fun useCard(card: Card, body: CardAction, player: Player) {
        if (card is AgentCard) {
            when(body.source) {
                Card.Source.HAND -> player.private.inPlayCards.add(card)
                Card.Source.PLAY -> player.private.inHandCards.add(card)
                Card.Source.DISCARD -> player.private.inHandCards.add(card)
                else -> {}
            }
        } else if (card is IntrigueCard) {
            when(body.source) {
                Card.Source.INTRIGUE -> player.private.usedIntrigues.add(card)
                else -> {}
            }
        }
    }

    private fun discardCard(card: AgentCard, body: CardAction, player: Player) {
        when(body.source) {
            Card.Source.HAND -> player.private.discardedCards.add(card)
            else -> {}
        }
    }

    fun handlePlaceAgent(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<PlaceAgent>(action.body)
        val game = getGame(gameId)

        val otherPlayers = game.players.filterNot { it.name == playerName }

        sendAgent(gameId, body.agentId, body.locationId)
        val updatedPlayer = game.players
            .find { it.name == playerName }
            ?.let { WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(it)) }

        val updatedLocation = game.locations
            .find { it.id == body.locationId }
            ?.let { WSActionResponse.Content(WSActionResponse.Type.UPDATE_LOCATION, mapper.toTree(it)) }

        val messages = arrayListOf<WSActionResponse.Message>()
        otherPlayers.forEach {
            messages.add(WSActionResponse.Message(WSActionResponse.SinglePlayer(it.name), updatedPlayer))
            messages.add(WSActionResponse.Message(WSActionResponse.SinglePlayer(it.name), updatedLocation))
        }

        return WSActionResponse(messages)
    }

    fun sendAgent(
        gameId: String,
        agentId: String,
        locationId: Int
    ) {
        val game = getGame(gameId)
        val location = game.locations.find { it.id == locationId }
        val player = game.players.find { player -> player.agents.any { a -> a.id == agentId } }
        if (location != null && player != null) {
            location.agents.add(Location.Agent(agentId, player.color.name, player.name))
            player.agents.removeAll { it.id == agentId }
        }
    }


    fun createGame(player: Player): Game {
        val gameId = "dune${games.size + 1}"
        val game = Game(gameId)
        games[gameId] = game
        game.addOrUpdatePlayer(player.name, player)
        return game
    }

    fun addPlayer(player: Player, gameId: String): Game {
        val game = getGame(gameId)
        game.addOrUpdatePlayer(player.name, player)
        return game
    }

    fun updatePlayer(player: Player, gameId: String): Game {
        val game = getGame(gameId)
        game.addOrUpdatePlayer(player.name, player)
        return game
    }

    fun getPlayer(gameId: String, playerName: String): Player {
        val game = getGame(gameId)
        return game.getPlayerByName(playerName)
            ?: throw ExpiredGameException("Player $playerName is not assigned to this game")
    }

    fun getGame(id: String): Game = games[id]
        ?: throw ExpiredGameException("Game not found. Create a game before retrieving it.")
}
