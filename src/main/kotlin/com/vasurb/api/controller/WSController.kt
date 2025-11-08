package com.vasurb.api.controller

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vasurb.api.model.Action.Type.*
import com.vasurb.api.model.ws_request.AddToGame
import com.vasurb.api.model.ws_request.WSActionRequest
import com.vasurb.api.model.ws_request.WSActionResponse
import com.vasurb.service.GameService
import com.vasurb.util.Util.getAs
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
        when (action.type) {
            ADD_TO_GAME -> addToGame(gameId, action)
            GET_CHARACTER_READY_STATES -> getPlayerReadyStates(gameId)
            START_GAME -> getStartGame(gameId)
            else -> println("Unknown action type: ${action.type}")
        }
    }

    private fun addToGame(
        gameId: String,
        action: WSActionRequest
    ) {
        val body = mapper.getAs<AddToGame>(action.body)
        val player = gameService.getPlayer(gameId, body.playerName)
        player.readyState = "READY"
        getPlayerReadyStates(gameId)
    }

    private fun getPlayerReadyStates(gameId: String) {
        val players = gameService.getGame(gameId).players

        val responseBody = mapper.createArrayNode().apply {
            players.forEach {
                add(mapper.createObjectNode().apply {
                    put("name", it.name)
                    put("color", it.color.name)
                    put("characterName", it.character.name)
                    putArray("characterUrls").apply {
                        it.character.urls.forEach { url -> add(url) }
                    }
                    put("avatarUrl", it.character.avatarUrl)
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
        simpMessagingTemplate.convertAndSend(
            "/topic/game/$gameId",
            WSActionResponse(WSActionResponse.Type.START_GAME, null)
        )
    }

}
