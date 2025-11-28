package com.vasurb.api.controller

import com.fasterxml.jackson.module.kotlin.readValue
import com.vasurb.api.model.Action.Type.*
import com.vasurb.api.model.ws_request.WSActionRequest
import com.vasurb.api.model.ws_request.WSActionResponse
import com.vasurb.service.GameService
import com.vasurb.util.Util.mapper
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

    @MessageMapping("/game/{gameId}")
    fun handleMessage(
        @DestinationVariable gameId: String,
        message: String,
        principal: Principal
    ) {
        val playerName = principal.name ?: throw RuntimeException("Missing Player name")
        val action: WSActionRequest = mapper.readValue(message)
        println("Received request: $action")

        val response: WSActionResponse? = when (action.type) {
            ADD_TO_GAME -> gameService.addToGame(gameId, action)
            GET_CHARACTER_READY_STATES -> gameService.getPlayerReadyStates(gameId)
            START_GAME -> gameService.getStartGame(gameId)
            RESUME_GAME -> gameService.getResumeGame(gameId, playerName)
            USE_CARD, DISCARD_CARD, TRASH_CARD -> gameService.handleCardAction(gameId, playerName, action)
            DRAW_CARD -> gameService.drawCard(gameId, playerName)
            PLACE_AGENT -> gameService.handlePlaceAgent(playerName, gameId, action)
            RECALL_AGENT -> gameService.handleRecallAgent(playerName, gameId, action)
            else -> null
        }

        response?.messages?.forEach { message ->
            if (message.content == null) return@forEach
            when (message.recipient) {
                is WSActionResponse.AllPlayers ->
                    sendMessageToAll(gameId, message.content)
                is WSActionResponse.SinglePlayer ->
                    sendMessageToUser(gameId, message.recipient.playerName, message.content)
                is WSActionResponse.AllPlayersExcept ->
                    gameService.getAllPlayersExcept(gameId, message.recipient.playerName)
                        .forEach { sendMessageToUser(gameId, it, message.content) }
            }
        }
    }

    private fun sendMessageToUser(gameId: String, userId: String, content: WSActionResponse.Content) {
        simpMessagingTemplate.convertAndSendToUser(userId, "/queue/game/${gameId}", content)
    }

    private fun sendMessageToAll(gameId: String, content: WSActionResponse.Content) {
        simpMessagingTemplate.convertAndSend("/topic/game/$gameId", content)
    }
}
