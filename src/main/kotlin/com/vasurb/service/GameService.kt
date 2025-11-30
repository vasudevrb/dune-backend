package com.vasurb.service

import com.vasurb.api.model.Action.Type.*
import com.vasurb.api.model.ws_request.*
import com.vasurb.exception.ExpiredGameException
import com.vasurb.model.*
import com.vasurb.model.Objective.*
import com.vasurb.util.Util.dec
import com.vasurb.util.Util.getAs
import com.vasurb.util.Util.getRandomAndRemove
import com.vasurb.util.Util.inc
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
                        it.character?.let { character -> character.urls.forEach { url -> add(url) } }
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
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.START_GAME, mapper.toTree(game))
            )
        )

        game.players.forEach {
            val response = WSActionResponse.Content(
                WSActionResponse.Type.UPDATE_PLAYER,
                mapper.toTree(it, includePrivate = true)
            )
            messages.add(
                WSActionResponse.Message(WSActionResponse.SinglePlayer(it.name), response)
            )
        }

        return WSActionResponse(messages)
    }

    fun getResumeGame(gameId: String, playerName: String): WSActionResponse? {
        val game = getGame(gameId)
        val playerInfo = game.players.find { it.name == playerName }
        return playerInfo?.let {
            WSActionResponse(
                listOf(
                    WSActionResponse.Message(
                        WSActionResponse.SinglePlayer(playerName),
                        WSActionResponse.Content(WSActionResponse.Type.START_GAME, mapper.toTree(game))
                    ),
                    WSActionResponse.Message(
                        WSActionResponse.SinglePlayer(playerName),
                        WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(playerInfo, includePrivate = true))
                    )
                )
            )
        }
    }

    fun drawCard(
        gameId: String,
        playerName: String
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)

        player.private.inHandCards.add(player.private.drawPile.draw())
        val message = "$playerName drew a card"

        return WSActionResponse(
            listOf(
                WSActionResponse.Message(
                    WSActionResponse.SinglePlayer(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
                ),
                WSActionResponse.Message(
                    WSActionResponse.AllPlayersExcept(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification(message)))
                )
            )
        )
    }

    fun drawIntrigueCard(
        gameId: String,
        playerName: String,
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)

        player.private.intrigueCards.add(game.intrigueCards.draw())
        val message = "$playerName drew an intrigue card"

        return WSActionResponse(
            listOf(
                WSActionResponse.Message(
                    WSActionResponse.SinglePlayer(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
                ),
                WSActionResponse.Message(
                    WSActionResponse.AllPlayersExcept(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification(message)))
                )
            )
        )
    }

    fun stealIntrigueCards(
        gameId: String,
        playerName: String,
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)

        val stealablePlayers = game.players.filter { it.name != playerName && it.private.intrigueCards.size > 3 }
        if (stealablePlayers.isEmpty()) {
            val msg = "No players have more than 3 intrigue cards."
            return WSActionResponse(
                listOf(
                    WSActionResponse.Message(
                        WSActionResponse.AllPlayers,
                        WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification(msg)))
                    )
                )
            )
        }

        stealablePlayers.forEach {
            val intrigue = it.private.intrigueCards.random()
            player.private.intrigueCards.add(intrigue)
            it.private.intrigueCards.remove(intrigue)
        }

        val notifyMessage = "$playerName stole intrigue cards from ${stealablePlayers.joinToString(", ") { it.name }}."
        val messages = arrayListOf(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        stealablePlayers.forEach {
            messages.add(
                WSActionResponse.Message(
                    WSActionResponse.SinglePlayer(it.name),
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(it, includePrivate = true))
                )
            )
        }

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification(notifyMessage)))
            )
        )

        return WSActionResponse(messages)
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
        val sourceList = when (body.source) {
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

            messages.add(
                WSActionResponse.Message(
                    WSActionResponse.SinglePlayer(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
                )
            )

            messages.add(
                WSActionResponse.Message(
                    WSActionResponse.AllPlayers,
                    WSActionResponse.Content(WSActionResponse.Type.CARD_USED, mapper.toTree(CardUsed(body.url, playerName, action.type)))
                )
            )
        }

        return WSActionResponse(messages)
    }

    private fun useCard(card: Card, body: CardAction, player: Player) {
        if (card is AgentCard) {
            when (body.source) {
                Card.Source.HAND -> player.private.inPlayCards.add(card)
                Card.Source.PLAY -> player.private.inHandCards.add(card)
                Card.Source.DISCARD -> player.private.inHandCards.add(card)
                else -> {}
            }
        } else if (card is IntrigueCard) {
            when (body.source) {
                Card.Source.INTRIGUE -> player.private.usedIntrigues.add(card)
                else -> {}
            }
        }
    }

    private fun discardCard(card: AgentCard, body: CardAction, player: Player) {
        when (body.source) {
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

        val location = game.locations.find { it.id == body.locationId }
        sendAgent(gameId, body.agentId, location)
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

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName sent an agent to ${location?.name}"))
                )
            )
        )
        return WSActionResponse(messages)
    }

    fun sendAgent(
        gameId: String,
        agentId: String,
        location: Location?
    ) {
        val game = getGame(gameId)
        val player = game.players.find { player -> player.agents.any { a -> a.id == agentId } }
        if (location != null && player != null) {
            location.agents.add(Location.Agent(agentId, player.color.name, player.name))
            player.agents.removeAll { it.id == agentId }
        }
    }

    fun handleRecallAgent(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<RecallAgent>(action.body)
        val game = getGame(gameId)
        val location = game.locations.find { it.agents.find { agent -> agent.agentId == body.agentId } != null }
        val otherPlayers = game.players.filterNot { it.name == playerName }
        recallAgent(gameId, playerName, location, body.agentId)

        val updatedPlayer = game.players
            .find { it.name == playerName }
            ?.let { WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(it)) }

        val updatedLocation = game.locations
            .find { it.id == location?.id }
            ?.let { WSActionResponse.Content(WSActionResponse.Type.UPDATE_LOCATION, mapper.toTree(it)) }

        val messages = arrayListOf<WSActionResponse.Message>()
        otherPlayers.forEach {
            messages.add(WSActionResponse.Message(WSActionResponse.SinglePlayer(it.name), updatedPlayer))
            messages.add(WSActionResponse.Message(WSActionResponse.SinglePlayer(it.name), updatedLocation))
        }

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName recalled an agent from ${location?.name}"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun recallAgent(
        gameId: String,
        playerName: String,
        location: Location?,
        agentId: String
    ) {
        val player = getPlayer(gameId, playerName)
        if (location != null) {
            location.agents.removeIf { agent -> agent.agentId == agentId }
            player.agents.add(Agent(agentId))
        }
    }

    fun handleMoveUnit(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<MoveCombatUnit>(action.body)

        when (action.destination) {
            CombatMovementDestination.Combat -> {
                player.combat.troopsInCombat += 1
                player.combat.strength += 2
                player.combat.troopsInGarrison -= 1
            }

            CombatMovementDestination.Garrison -> {
                player.combat.troopsInCombat -= 1
                player.combat.strength -= 2
                player.combat.troopsInGarrison += 1
            }

            CombatMovementDestination.Supply -> {
                player.combat.troopsInCombat -= 1
                player.combat.strength -= 2
            }
        }

        val responseBody = mapper.createObjectNode().apply {
            put("playerName", playerName)
            putPOJO("combat", player.combat)
        }

        val message = WSActionResponse.Message(
            WSActionResponse.AllPlayersExcept(playerName),
            WSActionResponse.Content(WSActionResponse.Type.UPDATE_COMBAT, responseBody)
        )

        return WSActionResponse(listOf(message))
    }

    fun handleAddOrRemoveCombatUnit(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AddOrRemoveCombatUnit>(action.body)

        when (action.unitType) {
            "Sandworm" -> {
                if (action.add){
                    player.combat.wormsInCombat++
                    player.combat.strength += 3
                } else {
                    player.combat.wormsInCombat--
                    player.combat.strength -= 3
                }
            }
            "Troop" -> if (action.add) player.combat.troopsInGarrison++ else player.combat.troopsInGarrison--
            "Strength" -> if (action.add) player.combat.strength++ else player.combat.strength--
        }

        val responseBody = mapper.createObjectNode().apply {
            put("playerName", playerName)
            putPOJO("combat", player.combat)
        }

        val message = WSActionResponse.Message(
            WSActionResponse.AllPlayersExcept(playerName),
            WSActionResponse.Content(WSActionResponse.Type.UPDATE_COMBAT, responseBody)
        )

        return WSActionResponse(listOf(
            message,
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName ${if (action.add) "increased" else "decreased"} their ${action.unitType}"))
                )
            )
        ))
    }

    fun handleAddOrRemoveResource(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AddOrRemoveResource>(action.body)

        when (action.resourceType) {
            Resource.water -> if(action.add) player.resources.inc(Resource.water) else player.resources.dec(Resource.water)
            Resource.spice -> if(action.add) player.resources.inc(Resource.spice) else player.resources.dec(Resource.spice)
            Resource.solari -> if(action.add) player.resources.inc(Resource.solari) else player.resources.dec(Resource.solari)
        }

        val responseBody = mapper.createObjectNode().apply {
            put("playerName", playerName)
            putPOJO("resources", player.resources)
        }

        val message = WSActionResponse.Message(
            WSActionResponse.AllPlayersExcept(playerName),
            WSActionResponse.Content(WSActionResponse.Type.UPDATE_RESOURCES, responseBody)
        )

        return WSActionResponse(listOf(
            message,
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName ${if (action.add) "gained" else "spent"} 1 ${action.resourceType}"))
                )
            )
        ))
    }

    fun endTurn(
        gameId: String
    ): WSActionResponse {
        val game = getGame(gameId)
        val currentPlayerIndex = game.players.indexOfFirst { it.name == game.currentPlayer }
        game.currentPlayer =
            if (currentPlayerIndex == game.players.size - 1) game.players[0].name
            else game.players[currentPlayerIndex + 1].name

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.START_GAME, mapper.toTree(game))
            )
        )

        return WSActionResponse(messages)
    }

    fun reveal(
        playerName: String,
        gameId: String,
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val inHandCardUrls = player.private.inHandCards.map { it.url }

        val iterator = player.private.inHandCards.iterator()

        while (iterator.hasNext()) {
            val card = iterator.next()
            useCard(card, CardAction(card.url, Card.Source.HAND), player)
            iterator.remove()
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.REVEAL_CARDS, mapper.toTree(RevealCards(inHandCardUrls, playerName)))
            )
        )

        messages.add(WSActionResponse.Message(
            WSActionResponse.SinglePlayer(playerName),
            WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
        ))

        return WSActionResponse(messages)
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

    fun getAllPlayersExcept(gameId: String, playerName: String): List<String> {
        return getGame(gameId)
            .players
            .map { it.name }
            .filterNot { it == playerName }
    }

    fun getGame(id: String): Game = games[id]
        ?: throw ExpiredGameException("Game not found. Create a game before retrieving it.")
}
