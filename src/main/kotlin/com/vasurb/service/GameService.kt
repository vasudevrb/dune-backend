package com.vasurb.service

import com.vasurb.api.controller.GameController.Companion.NUM_PICKABLE_CHARACTERS
import com.vasurb.api.model.Action.Type.*
import com.vasurb.api.model.ws_request.*
import com.vasurb.exception.ExpiredGameException
import com.vasurb.model.*
import com.vasurb.model.Card.Source.BLOODLINES
import com.vasurb.model.Card.Source.UPRISING
import com.vasurb.model.Objective.*
import com.vasurb.util.Util.dec
import com.vasurb.util.Util.getAs
import com.vasurb.util.Util.getRandomAndRemove
import com.vasurb.util.Util.inc
import com.vasurb.util.Util.mapper
import com.vasurb.util.Util.toTree
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

@Component
class GameService {

    private val games = ConcurrentHashMap<String, Game>()

    fun addToGame(
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<AddToGame>(action.body)
        val player = getPlayer(gameId, body.playerName)

        player.readyState = "READY"
        return getPlayerReadyStates(gameId)
    }

    fun getPlayerReadyStates(gameId: String): WSActionResponse {
        val players = getGame(gameId).players

        val responseBody = mapper.createArrayNode().apply {
            players.forEach {
                add(mapper.createObjectNode().apply {
                    put("playerName", it.name)
                    put("color", it.color?.name)
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

        game.players.sortBy { getTurnOrder(gameId, it.name) }
        val firstPlayer = game.players[0]
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
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
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
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
                ),
                WSActionResponse.Message(
                    WSActionResponse.AllPlayersExcept(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification(message)))
                )
            )
        )
    }

    // Used for rivals
    fun trashIntrigueCard(
        gameId: String,
        playerName: String,
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)

        if (player.private.intrigueCards.isEmpty()){
            return WSActionResponse(listOf())
        }
        player.private.intrigueCards.removeAt(0)
        val message = "$playerName trashed an intrigue card"

        return WSActionResponse(
            listOf(
                WSActionResponse.Message(
                    WSActionResponse.SinglePlayer(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
                ),
                WSActionResponse.Message(
                    WSActionResponse.AllPlayersExcept(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
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

        val stealablePlayers = game.players.filter {
            val intrigueHandSize = if (it.techs.find { t -> t.url.contains("tech_2") } != null) 6 else 3
            it.name != playerName && it.private.intrigueCards.size > intrigueHandSize
        }
        if (stealablePlayers.isEmpty()) {
            val msg = "No players have more than 3 or 5 (Tech) intrigue cards."
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

            messages.add(
                WSActionResponse.Message(
                    WSActionResponse.AllPlayersExcept(it.name),
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
                )
            )
        }

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification(notifyMessage)))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
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
        val sourceDeckList = when (body.source) {
            Card.SourceDeck.HAND -> player.private.inHandCards
            Card.SourceDeck.PLAY -> player.private.inPlayCards
            Card.SourceDeck.DISCARD -> player.private.discardedCards
            Card.SourceDeck.INTRIGUE -> player.private.intrigueCards
        }

        val removable = sourceDeckList.find { it.url == body.url }
        val messages = arrayListOf<WSActionResponse.Message>()

        removable?.let {
            sourceDeckList.remove(it)
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

            messages.add(
                WSActionResponse.Message(
                    WSActionResponse.AllPlayersExcept(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
                )
            )
        }

        return WSActionResponse(messages)
    }

    private fun useCard(card: Card, body: CardAction, player: Player) {
        if (card is AgentCard) {
            when (body.source) {
                Card.SourceDeck.HAND -> player.private.inPlayCards.add(card)
                Card.SourceDeck.PLAY -> player.private.inHandCards.add(card)
                Card.SourceDeck.DISCARD -> player.private.inHandCards.add(card)
                else -> {}
            }
        } else if (card is IntrigueCard) {
            when (body.source) {
                Card.SourceDeck.INTRIGUE -> player.private.usedIntrigues.add(card)
                else -> {}
            }
        }
    }

    private fun discardCard(card: AgentCard, body: CardAction, player: Player) {
        when (body.source) {
            Card.SourceDeck.HAND -> player.private.discardedCards.add(card)
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

    fun handlePlaceControlFlag(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<PlaceControlFlag>(action.body)
        val game = getGame(gameId)

        val location = game.locations.find { it.id == body.locationId }
        sendControlFlag(gameId, body.controlFlagId, location)
        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName gained control of ${location?.name}"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun sendControlFlag(
        gameId: String,
        controlFlagId: String,
        location: Location?
    ) {
        val game = getGame(gameId)
        val player = game.players.find { player -> player.controlFlags.any { cf -> cf.id == controlFlagId } }
        if (location != null && player != null) {
            if (location.controlFlag != null) {
                val prevControlPlayer = getPlayer(gameId, location.controlFlag!!.playerName)
                prevControlPlayer.controlFlags.add(ControlFlag(location.controlFlag!!.controlFlagId))
            }

            location.controlFlag = Location.ControlFlag(controlFlagId,  player.color.name, player.name)
            player.controlFlags.removeAll { it.id == controlFlagId }
        }
    }

    fun handleRecallControlFlag(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<RecallControlFlag>(action.body)
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val location = game.locations.find { it.controlFlag?.controlFlagId == body.controlFlagId }
        if (location != null) {
            location.controlFlag = null
            player.controlFlags.add(ControlFlag(body.controlFlagId))
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName lost control of ${location?.name}"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun handleSendSpy(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<PlaceSpy>(action.body)
        val game = getGame(gameId)

        val otherPlayers = game.players.filterNot { it.name == playerName }

        val location = game.spyLocations.find { it.id == body.spyLocationId }
        sendSpy(gameId, body.spyId, location)
        val updatedPlayer = game.players
            .find { it.name == playerName }
            ?.let { WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(it)) }

        val updatedLocation = game.spyLocations
            .find { it.id == body.spyLocationId }
            ?.let { WSActionResponse.Content(WSActionResponse.Type.UPDATE_SPY_LOCATION, mapper.toTree(it)) }

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
                    mapper.toTree(Notification("$playerName sent a spy"))
                )
            )
        )
        return WSActionResponse(messages)
    }

    fun sendSpy(
        gameId: String,
        spyId: String,
        spyLocation: SpyLocation?
    ) {
        val game = getGame(gameId)
        val player = game.players.find { player -> player.spies.any { s -> s.id == spyId } }
        if (spyLocation != null && player != null) {
            spyLocation.spies.add(SpyLocation.Spy(spyId, player.color.name, player.name))
            player.spies.removeAll { it.id == spyId }
        }
    }

    fun handleRecallSpy(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val body = mapper.getAs<RecallSpy>(action.body)
        val game = getGame(gameId)
        val spyLocation = game.spyLocations.find { it.spies.find { spy -> spy.spyId == body.spyId } != null }
        val otherPlayers = game.players.filterNot { it.name == playerName }
        recallSpy(gameId, playerName, spyLocation, body.spyId)

        val updatedPlayer = game.players
            .find { it.name == playerName }
            ?.let { WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(it)) }

        val updatedLocation = game.spyLocations
            .find { it.id == spyLocation?.id }
            ?.let { WSActionResponse.Content(WSActionResponse.Type.UPDATE_SPY_LOCATION, mapper.toTree(it)) }

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
                    mapper.toTree(Notification("$playerName recalled a spy"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun recallSpy(
        gameId: String,
        playerName: String,
        spyLocation: SpyLocation?,
        spyId: String
    ) {
        val player = getPlayer(gameId, playerName)
        if (spyLocation != null) {
            spyLocation.spies.removeIf { spy -> spy.spyId == spyId }
            player.spies.add(Spy(spyId))
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
                if (action.unitType == "Commander") {
                    player.combat.commandersInCombat += 1
                    player.combat.strength += 2
                    player.combat.commandersInGarrison -= 1
                } else {
                    player.combat.troopsInCombat += 1
                    player.combat.strength += 2
                    player.combat.troopsInGarrison -= 1
                }
            }

            CombatMovementDestination.Garrison -> {
                if (action.unitType == "Commander") {
                    player.combat.commandersInCombat -= 1
                    player.combat.strength -= 2
                    player.combat.commandersInGarrison += 1
                } else {
                    player.combat.troopsInCombat -= 1
                    player.combat.strength -= 2
                    player.combat.troopsInGarrison += 1
                }
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

        when (action.add) {
            true -> {
                when (action.unitType) {
                    "Sandworm" -> {
                        player.combat.wormsInCombat+= action.quantity
                        player.combat.strength += (3 * action.quantity)
                    }
                    "Troop" -> {
                        player.combat.troopsInGarrison += action.quantity
                        player.combat.troopsInSupply -= action.quantity
                    }
                    "Commander" -> {
                        player.combat.commandersInGarrison += action.quantity
                        player.combat.commandersInSupply -= action.quantity
                    }
                    "Strength" -> player.combat.strength += action.quantity
                }
            }
            false -> {
                when (action.unitType) {
                    "Sandworm" -> {
                        val quantity = min(player.combat.wormsInCombat, action.quantity)
                        player.combat.wormsInCombat-= quantity
                        player.combat.strength -= (3 * quantity)
                    }
                    "Troop" -> {
                        val quantity = min(player.combat.troopsInGarrison, action.quantity)
                        player.combat.troopsInGarrison -= quantity
                        player.combat.troopsInSupply += quantity
                    }
                    "Commander" -> {
                        val quantity = min(player.combat.commandersInGarrison, action.quantity)
                        player.combat.commandersInGarrison -= quantity
                        player.combat.commandersInSupply += quantity
                    }
                    "Strength" -> {
                        val quantity = min(player.combat.strength, action.quantity)
                        player.combat.strength -= quantity
                    }
                }
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

        var quantity = action.quantity
        if (action.add) {
            player.resources.inc(action.resourceType, quantity)
        } else {
            quantity = min(player.resources.getValue(action.resourceType), action.quantity)
            player.resources.dec(action.resourceType, quantity)
        }

        val responseBody = mapper.createObjectNode().apply {
            put("playerName", playerName)
            putPOJO("resources", player.resources)
        }

        val messages = mutableListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName), WSActionResponse.Content(WSActionResponse.Type.UPDATE_RESOURCES, responseBody)
            )
        )

        if (quantity > 0) {
            messages.add(
                WSActionResponse.Message(
                    WSActionResponse.AllPlayersExcept(playerName), WSActionResponse.Content(
                        WSActionResponse.Type.SHOW_NOTIFICATION,
                        mapper.toTree(Notification("$playerName ${if (action.add) "gained" else "spent"} $quantity ${action.resourceType}"))
                    )
                )
            )
        }

        return WSActionResponse(messages)
    }

    fun addOrRemoveVP(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AddOrRemoveVP>(action.body)
        if (action.add) player.victoryPoints++
        else player.victoryPoints--

        val message = WSActionResponse.Message(
            WSActionResponse.AllPlayersExcept(playerName),
            WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
        )

        return WSActionResponse(listOf(
            message,
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName ${if (action.add) "gained" else "lost"} a victory point"))
                )
            )
        ))
    }

    fun acquireImperiumCard(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AcquireImperiumCard>(action.body)
        val card = game.imperiumRow.find { it.url == action.url }
        val cardIndex = game.imperiumRow.indexOf(card)
        if (card != null) {
            player.private.discardedCards.add(card)
            game.imperiumRow.remove(card)
            game.imperiumRow.add(cardIndex, game.imperiumCards.draw())
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName acquired a card from the Imperium Row"))
                )
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        return WSActionResponse(messages)
    }

    fun acquireReserveCard(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AcquireImperiumCard>(action.body)
        val reserveCard = game.reserveCards.filter { (_, v) -> v.find { it.url == action.url } != null }
        reserveCard.forEach { (_, v) ->
            player.private.discardedCards.add(v.removeAt(0))
        }

        game.reserveRow = game.refreshReserveRow()
        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName acquired a card from the Reserve Row"))
                )
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        return WSActionResponse(messages)
    }

    fun acquireContract(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse{
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AcquireContract>(action.body)

        val index = game.currentContracts.indexOf(action.url)
        if (index != -1) {
            player.contracts.add(Contract(action.url))
            game.currentContracts.removeAt(index)
            game.currentContracts.add(index, game.contracts.draw().url)
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName acquired a contract"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun completeContract(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<CompleteContract>(action.body)

        val contract = player.contracts.find { it.url == action.url }
        val index = player.contracts.indexOf(contract)
        if (index != -1) {
            contract?.completed = action.completed
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName completed a contract"))
                )
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        return WSActionResponse(messages)
    }

    fun breakShieldWall(
        playerName: String,
        gameId: String
    ): WSActionResponse {
        val game = getGame(gameId)
        game.shieldWallBroken = true

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName destroyed the shield wall."))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun gainOrLoseAlliance(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<GainOrLoseAlliance>(action.body)
        if (action.gained) player.factionAlliances.add(action.type) else player.factionAlliances.remove(action.type)

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName ${if(action.gained) "gained" else "lost"} the ${action.type} alliance"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun gainOrLoseObjective(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<GainOrLoseObjective>(action.body)
        if (action.gained) player.objectives.add(action.type) else player.objectives.remove(action.type)

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName ${if (action.gained) "gained" else "lost"} the ${action.type} objective"))
                )
            )
        )

        return WSActionResponse(messages)
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
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        return WSActionResponse(messages)
    }

    fun clearRound(
        gameId: String,
    ): WSActionResponse {
        val game = getGame(gameId)

        val firstPlayerIndex = game.players.indexOfFirst { it.name == game.firstPlayer }
        game.firstPlayer =
            if (firstPlayerIndex == game.players.size - 1) game.players[0].name
            else game.players[firstPlayerIndex + 1].name

        game.currentPlayer = game.firstPlayer

        game.players.forEach {
            it.combat.troopsInCombat = 0
            it.combat.wormsInCombat = 0
            it.combat.strength = 0
            it.combat.commandersInSupply += it.combat.commandersInCombat
            it.combat.commandersInCombat = 0

            val duncanAgent = it.character?.additionalInfo?.duncanAgentDeployed
            if (duncanAgent != null) {
                it.agents.add(duncanAgent)
                it.character?.additionalInfo?.duncanAgentDeployed = null
            }

            it.techs.forEach { tt -> tt.flipped = false }
            it.private.discardedCards.addAll(it.private.inPlayCards)
            it.private.inPlayCards.clear()
            it.private.discardedCards.addAll(it.private.inHandCards)
            it.private.inHandCards.clear()
            it.private.inHandCards.addAll(it.private.drawPile.draw(5))
        }

        listOf(9, 10, 11)
            .map { locationId -> game.locations.find { it.id == locationId }}
            .mapNotNull { it }
            .filter { it.agents.isEmpty() }
            .forEach { loc ->
                when(loc.id) {
                    9 -> game.bonusSpice.deepDesert++
                    10 -> game.bonusSpice.haggaBasin++
                    11 -> game.bonusSpice.imperialBasin++
                    23 -> game.bonusSpice.tueksSietch++
                }
            }

        game.currentConflict = game.conflictCards.draw().url
        game.nextConflictLevel = game.conflictCards.peek()?.conflictType?.level ?: 0

        game.locations.forEach { location ->
            val agentIterator = location.agents.iterator()
            while (agentIterator.hasNext()) {
                val agent = agentIterator.next()
                getPlayer(gameId, agent.playerName).agents.add(Agent(agent.agentId))
                agentIterator.remove()
            }
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
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

    fun reveal(
        playerName: String,
        gameId: String,
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val inHandCardUrls = player.private.inHandCards.map { it.url }

        val iterator = player.private.inHandCards.iterator()

        while (iterator.hasNext()) {
            val card = iterator.next()
            useCard(card, CardAction(card.url, Card.SourceDeck.HAND), player)
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

    fun setFactionInfluence(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<SetFactionInfluence>(action.body)

        player.factionInfluences[action.factionType] = action.influenceLevel

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName set their ${action.factionType} influence to  ${action.influenceLevel}"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun setFeydSignetStatus(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<SetFeydSignetStatus>(action.body)
        val character = player.character
        if (character != null) {
            character.additionalInfo.signetStatus = action.status
        }

        return WSActionResponse(listOf())
    }

    fun unlockSwordmaster(
        playerName: String,
        gameId: String,
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        player.swordmasterUnlocked = true

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName unlocked their swordmaster"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun getHighCouncil(
        playerName: String,
        gameId: String,
    ): WSActionResponse {
        val game = getGame(gameId)
        val freeSeatIndex = game.highCouncil.indexOfFirst { it.isEmpty() }

        game.highCouncil[freeSeatIndex] = playerName

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName took a High Council seat"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun unlockMakerHook(
        playerName: String,
        gameId: String,
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        player.makerHookUnlocked = true

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName unlocked their maker hook"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun getNextConflict(
        gameId: String,
    ): WSActionResponse {
        val game = getGame(gameId)
        game.currentConflict = game.conflictCards.draw().url
        game.nextConflictLevel = game.conflictCards.peek()?.conflictType?.level ?: 0

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        return WSActionResponse(messages)
    }

    fun setBonusSpice(
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val action = mapper.getAs<BonusSpiceAction>(action.body)

        when(action.locationId) {
            9 -> if(action.add) game.bonusSpice.deepDesert++ else game.bonusSpice.deepDesert--
            10 -> if(action.add) game.bonusSpice.haggaBasin++ else game.bonusSpice.haggaBasin--
            11 -> if(action.add) game.bonusSpice.imperialBasin++ else game.bonusSpice.imperialBasin--
            23 -> if(action.add) game.bonusSpice.tueksSietch++ else game.bonusSpice.tueksSietch--
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        val msg = "Bonus spice added at ${game.locations.find { it.id == action.locationId }?.name}"
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification(msg)))
            )
        )

        return WSActionResponse(messages)
    }

    fun getHagalCard(
        rivalPlayerName: String,
        gameId: String,
    ): WSActionResponse {
        val game = getGame(gameId)
        val card = game.hagalCards.draw()
        game.usedHagalCards.add(card)

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.CARD_USED, mapper.toTree(CardUsed(card.url, rivalPlayerName, DRAW_CARD)))
            )
        )

        return WSActionResponse(messages)
    }

    fun reshuffleHagalCards(
        gameId: String
    ): WSActionResponse {
        val game = getGame(gameId)
        game.hagalCards.reshuffleAll()

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification("Hagal deck was reshuffled")))
            )
        )

        return WSActionResponse(messages)
    }

    fun acquireSardaukarCommander(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AcquireSardaukarCommanderAction>(action.body)

        val commander = game.sardaukarCommanders.find { it.id == action.commanderId }
        if (commander !== null) {
            val player = getPlayer(gameId, playerName)
            player.combat.commandersInSupply++
            game.sardaukarCommanders.removeIf { it.id == action.commanderId }
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        val msg = "$playerName acquired a Sardaukar Commander from ${game.locations.find { it.id == action.commanderId }?.name}"
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.SHOW_NOTIFICATION, mapper.toTree(Notification(msg)))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        return WSActionResponse(messages)
    }

    fun acquireCommanderSkill(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AcquireCommanderSkillAction>(action.body)

        val index = game.currentSkills.indexOfFirst { it.url == action.url }
        if (index != -1) {
            player.skills.add(game.currentSkills[index])
            game.currentSkills.removeAt(index)
            game.currentSkills.add(index, game.skills.draw())
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName acquired a Sardaukar Skill"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun trashCommanderSkill(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<TrashCommanderSkillAction>(action.body)

        val index = player.skills.indexOfFirst { it.url == action.url }
        if (index != -1) {
            player.skills.removeAt(index)
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName trashed a Sardaukar Skill"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun acquireTechTile(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<AcquireTechAction>(action.body)

        val index = game.currentTechs.indexOfFirst { it.url == action.url }
        if (index != -1) {
            player.techs.add(game.currentTechs[index])
            game.currentTechs.removeAt(index)
            game.currentTechs.add(index, game.techs.draw())
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName acquired a tech"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun flipTechTile(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<FlipTechAction>(action.body)

        val tech = player.techs.find { it.url == action.url }
        tech?.flipped = action.flipped

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName flipped a tech"))
                )
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.CARD_USED, mapper.toTree(CardUsed(action.url, playerName, DRAW_CARD)))
            )
        )

        return WSActionResponse(messages)
    }


    fun trashTechTile(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<TrashTechAction>(action.body)

        val index = player.techs.indexOfFirst { it.url == action.url }
        if (index != -1) {
            player.techs.removeAt(index)
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName trashed a tech"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun deployDuncanAgent(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ): WSActionResponse {
        val game = getGame(gameId)
        val player = getPlayer(gameId, playerName)
        val action = mapper.getAs<DeployDuncanAgentAction>(action.body)

        val location = game.locations.find { it.agents.find { agent -> agent.agentId == action.agentId } != null }
        recallAgent(gameId, playerName, location, action.agentId)

        val agent = player.agents.find { it.id == action.agentId }
        if (agent != null) {
            player.agents.remove(agent)
            player.character?.additionalInfo?.duncanAgentDeployed = agent
        }

        val messages = arrayListOf<WSActionResponse.Message>()
        messages.add(
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player, includePrivate = true))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayers,
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_GAME, mapper.toTree(game))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(WSActionResponse.Type.UPDATE_PLAYER, mapper.toTree(player))
            )
        )

        messages.add(
            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName deployed an agent to combat"))
                )
            )
        )

        return WSActionResponse(messages)
    }

    fun peekDeckCard(
        playerName: String,
        gameId: String
    ): WSActionResponse {
        val player = getPlayer(gameId, playerName)
        val nextCard = player.private.drawPile.peek()

        val messages = arrayListOf<WSActionResponse.Message>()

        if (nextCard != null) {
            messages.add(
                WSActionResponse.Message(
                    WSActionResponse.SinglePlayer(playerName),
                    WSActionResponse.Content(WSActionResponse.Type.CARD_USED, mapper.toTree(CardUsed(nextCard.url, playerName, DRAW_CARD)))
                )
            )

            WSActionResponse.Message(
                WSActionResponse.AllPlayersExcept(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("$playerName peeked at their next card"))
                )
            )
        } else {
            WSActionResponse.Message(
                WSActionResponse.SinglePlayer(playerName),
                WSActionResponse.Content(
                    WSActionResponse.Type.SHOW_NOTIFICATION,
                    mapper.toTree(Notification("Your deck is empty"))
                )
            )
        }

        return WSActionResponse(messages)
    }

    fun createGame(playerName: String, includeRivals: Boolean, includeBloodlines: Boolean): Game {
        val gameId = "dune${games.size + 1}"

        val sources = arrayListOf(UPRISING)
        if (includeBloodlines) sources.add(BLOODLINES)

        val game = Game(gameId, sources)
        game.containsRivals = includeRivals
        games[gameId] = game

        addPlayer(playerName, gameId, isHost = true)
        return game
    }

    fun addPlayer(playerName: String, gameId: String, isHost: Boolean = false, isRival: Boolean = false): Game {
        val game = getGame(gameId)
        val newPlayer = Player(playerName, isHost = isHost, isRival = isRival)

        newPlayer.color = game.availableColors.removeAt(0)
        val objective = getObjective(game)
        newPlayer.objectives.add(objective)

        if (game.initialTurnOrder.getValue(1).isEmpty() && objective == DesertMouse) {
            game.initialTurnOrder[1] = playerName
        } else {
            val availableTurnOrders = game.initialTurnOrder.filter { it.value.isEmpty() }.keys
            game.initialTurnOrder[availableTurnOrders.random()] = playerName
        }

        game.addOrUpdatePlayer(playerName, newPlayer)
        return game
    }

    private fun getObjective(game: Game): Objective {
        if (game.players.size <= 2) {
            return game.availableObjectives.getRandomAndRemove()
        } else {
            val FourPObjectives = arrayListOf(DesertMouse, Crysknife)

            val ornithopterPlayer = game.players.find { it.objectives.contains(Ornithopter) }
            ornithopterPlayer?.objectives = arrayListOf(FourPObjectives.getRandomAndRemove())

            return FourPObjectives[0]
        }
    }

    fun updatePlayer(player: Player, gameId: String): Game {
        val game = getGame(gameId)
        game.addOrUpdatePlayer(player.name, player)
        return game
    }

    fun getTurnOrder(gameId: String, playerName: String): Int {
        return getGame(gameId).initialTurnOrder.entries
            .find { it.value == playerName }
            ?.key ?: 0
    }

    fun getPresentableCharacters(playerName: String, gameId: String): List<PlayableCharacter> {
        val game = getGame(gameId)
        println("Current characters: ${game.availableCharacters.size}: ${game.availableCharacters}")
        val characters = game.presentedCharacters[playerName] ?: game.availableCharacters
            .shuffled()
            .take(if(game.containsRivals) game.availableCharacters.size else NUM_PICKABLE_CHARACTERS)

        characters.forEach { game.availableCharacters.remove(it) }
        println("For player ${playerName} returning: ${characters}. New size ${game.availableCharacters.size} : ${game.availableCharacters}")
        if (!game.containsRivals) { game.presentedCharacters[playerName] = characters }
        return characters
    }

    fun getRivals(): List<RivalCharacter> {
        return RivalCharacter.entries.toMutableList()
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
