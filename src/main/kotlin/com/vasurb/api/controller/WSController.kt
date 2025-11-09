package com.vasurb.api.controller

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vasurb.api.model.Action.Type.*
import com.vasurb.api.model.ws_request.AddToGame
import com.vasurb.api.model.ws_request.PlaceAgent
import com.vasurb.api.model.ws_request.WSActionRequest
import com.vasurb.api.model.ws_request.WSActionResponse
import com.vasurb.model.Game
import com.vasurb.model.Objective.Crysknife
import com.vasurb.model.Objective.DesertMouse
import com.vasurb.model.Objective.Ornithopter
import com.vasurb.model.Player
import com.vasurb.service.GameService
import com.vasurb.util.Util.convertAndSendToUser
import com.vasurb.util.Util.getAs
import com.vasurb.util.Util.getRandomAndRemove
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import java.security.Principal

@CrossOrigin(origins = ["http://localhost:5173"])
@Controller
class WSController(
    val gameService: GameService,
    val simpMessagingTemplate: SimpMessagingTemplate
) {

    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @MessageMapping("/game/{gameId}")
    fun handleMessage(
        @DestinationVariable gameId: String,
        message: String,
        principal: Principal
    ) {
        val playerName = principal.name ?: throw RuntimeException("Missing Player name")
        val action: WSActionRequest = mapper.readValue(message)
        println("Received request: $action")
        when (action.type) {
            ADD_TO_GAME -> addToGame(gameId, action)
            GET_CHARACTER_READY_STATES -> getPlayerReadyStates(gameId)
            START_GAME -> getStartGame(gameId)
            RESUME_GAME -> getResumeGame(gameId, playerName)
            PLACE_AGENT -> handlePlaceAgent(playerName, gameId, action)
            else -> println("Unknown action type: ${action.type}")
        }
    }

    private fun addToGame(
        gameId: String,
        action: WSActionRequest
    ) {
        val body = mapper.getAs<AddToGame>(action.body)
        val game = gameService.getGame(gameId)
        val player = gameService.getPlayer(gameId, body.playerName)
        assignObjective(game, player)

        player.readyState = "READY"
        getPlayerReadyStates(gameId)
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

    private fun getPlayerReadyStates(gameId: String) {
        val players = gameService.getGame(gameId).players

        val responseBody = mapper.createArrayNode().apply {
            players.forEach {
                add(mapper.createObjectNode().apply {
                    put("playerName", it.name)
                    put("color", it.color.name)
                    put("characterName", it.character.name)
                    putArray("characterUrls").apply {
                        it.character.urls.forEach { url -> add(url) }
                    }
                    put("avatarUrl", it.character.avatarUrl)
                    put("objective", it.objectives[0].name)
                    put("status", if (it.readyState != null) "Ready" else "Not ready")
                })
            }
        }

        val response = WSActionResponse(
            WSActionResponse.Type.GET_CHARACTER_READY_STATES,
            responseBody
        )

        simpMessagingTemplate.convertAndSend("/topic/game/$gameId", response)
    }

    private fun getStartGame(gameId: String) {
        val game = gameService.getGame(gameId)
        game.isStarted = true

        val desertMousePlayers = game.players.filter { it.objectives.contains(DesertMouse) }
        val firstPlayer = desertMousePlayers.random()
        game.players.remove(firstPlayer)
        game.players.add(0, firstPlayer)

        game.firstPlayer = firstPlayer.name
        game.currentPlayer = firstPlayer.name

        simpMessagingTemplate.convertAndSend(
            "/topic/game/$gameId",
            WSActionResponse(WSActionResponse.Type.START_GAME, mapper.valueToTree(game))
        )
    }

    private fun getResumeGame(gameId: String, playerName: String) {
        val game = gameService.getGame(gameId)
        simpMessagingTemplate.convertAndSendToUser(
            playerName,
            "/queue/game/$gameId",
            WSActionResponse(WSActionResponse.Type.START_GAME, mapper.valueToTree(game))
        )
    }

    private fun handlePlaceAgent(
        playerName: String,
        gameId: String,
        action: WSActionRequest
    ) {
        val body = mapper.getAs<PlaceAgent>(action.body)
        val game = gameService.getGame(gameId)

        val otherPlayers = game.players.filterNot { it.name == playerName }

        gameService.sendAgent(gameId, body.agentId, body.locationId)
        val updatedPlayer = game.players
            .find { it.name == playerName }
            ?.let { WSActionResponse(WSActionResponse.Type.UPDATE_PLAYER, mapper.valueToTree(it)) }

        val updatedLocation = game.locations
            .find { it.id == body.locationId }
            ?.let { WSActionResponse(WSActionResponse.Type.UPDATE_LOCATION, mapper.valueToTree(it)) }

        otherPlayers.forEach {
            simpMessagingTemplate.convertAndSendToUser(it.name, "/queue/game/${gameId}", updatedPlayer)
            simpMessagingTemplate.convertAndSendToUser(it.name, "/queue/game/${gameId}", updatedLocation)
        }
    }

}
