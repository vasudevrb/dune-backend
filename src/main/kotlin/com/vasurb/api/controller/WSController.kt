package com.vasurb.api.controller

import com.fasterxml.jackson.module.kotlin.readValue
import com.vasurb.api.model.Action
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
        val action: WSActionRequest = mapper.readValue(message)
        val playerName = getNameFromMessage(action)
            ?: principal.name
            ?: throw RuntimeException("Missing Player name")

        println("Received request: $action")

        val response: WSActionResponse? = when (action.type) {
            ADD_TO_GAME -> gameService.addToGame(gameId, action)
            GET_CHARACTER_READY_STATES -> gameService.getPlayerReadyStates(gameId)
            START_GAME -> gameService.getStartGame(gameId)
            RESUME_GAME -> gameService.getResumeGame(gameId, playerName)
            USE_CARD, DISCARD_CARD, TRASH_CARD -> gameService.handleCardAction(gameId, playerName, action)
            DRAW_CARD -> gameService.drawCard(gameId, playerName)
            GAIN_INTRIGUE_CARD -> gameService.drawIntrigueCard(gameId, playerName)
            TRASH_INTRIGUE_CARD -> gameService.trashIntrigueCard(gameId, playerName)
            STEAL_INTRIGUE_CARD -> gameService.stealIntrigueCards(gameId, playerName)
            PLACE_AGENT -> gameService.handlePlaceAgent(playerName, gameId, action)
            RECALL_AGENT -> gameService.handleRecallAgent(playerName, gameId, action)
            PLACE_SPY -> gameService.handleSendSpy(playerName, gameId, action)
            RECALL_SPY -> gameService.handleRecallSpy(playerName, gameId, action)
            PLACE_CONTROL_FLAG -> gameService.handlePlaceControlFlag(playerName, gameId, action)
            RECALL_CONTROL_FLAG -> gameService.handleRecallControlFlag(playerName, gameId, action)
            MOVE_COMBAT_UNIT -> gameService.handleMoveUnit(playerName, gameId, action)
            ADD_OR_REMOVE_COMBAT_UNIT -> gameService.handleAddOrRemoveCombatUnit(playerName, gameId, action)
            ADD_OR_REMOVE_RESOURCE -> gameService.handleAddOrRemoveResource(playerName, gameId, action)
            ADD_OR_REMOVE_VP -> gameService.addOrRemoveVP(playerName, gameId, action)
            END_TURN -> gameService.endTurn(gameId)
            REVEAL -> gameService.reveal(playerName, gameId)
            SET_FACTION_INFLUENCE -> gameService.setFactionInfluence(playerName, gameId, action)
            SET_FEYD_SIGNET_STATUS -> gameService.setFeydSignetStatus(playerName, gameId, action)
            UNLOCK_SWORDMASTER -> gameService.unlockSwordmaster(playerName, gameId)
            GET_HIGH_COUNCIL -> gameService.getHighCouncil(playerName, gameId)
            UNLOCK_MAKER_HOOK -> gameService.unlockMakerHook(playerName, gameId)
            GET_NEXT_CONFLICT -> gameService.getNextConflict(gameId)
            SET_BONUS_SPICE -> gameService.setBonusSpice(gameId, action)
            ACQUIRE_IMPERIUM_CARD -> gameService.acquireImperiumCard(playerName, gameId, action)
            ACQUIRE_RESERVE_CARD -> gameService.acquireReserveCard(playerName, gameId, action)
            GAIN_OR_LOSE_ALLIANCE -> gameService.gainOrLoseAlliance(playerName, gameId, action)
            GAIN_OR_LOSE_OBJECTIVE -> gameService.gainOrLoseObjective(playerName, gameId, action)
            ACQUIRE_CONTRACT -> gameService.acquireContract(playerName, gameId, action)
            COMPLETE_CONTRACT -> gameService.completeContract(playerName, gameId, action)
            BREAK_SHIELD_WALL -> gameService.breakShieldWall(playerName, gameId)
            GET_HAGAL_CARD -> gameService.getHagalCard(playerName, gameId)
            RESHUFFLE_HAGAL_CARDS -> gameService.reshuffleHagalCards(gameId)
            ACQUIRE_SARDAUKAR_COMMANDER -> gameService.acquireSardaukarCommander(playerName, gameId, action)
            ACQUIRE_COMMANDER_SKILL -> gameService.acquireCommanderSkill(playerName, gameId, action)
            TRASH_COMMANDER_SKILL -> gameService.trashCommanderSkill(playerName, gameId, action)
            ACQUIRE_TECH_TILE -> gameService.acquireTechTile(playerName, gameId, action)
            FLIP_TECH_TILE -> gameService.flipTechTile(playerName, gameId, action)
            TRASH_TECH_TILE -> gameService.trashTechTile(playerName, gameId, action)
            DEPLOY_DUNCAN_AGENT -> gameService.deployDuncanAgent(playerName, gameId, action)
            PEEK_DECK_CARD -> gameService.peekDeckCard(playerName, gameId)
            USE_FAMILY_ATOMICS -> gameService.useFamilyAtomics(playerName, gameId)
            SELECT_YRKOON_NAVIGATION_CARD -> gameService.selectYrkoonNavigationCard(playerName, gameId, action)
            CLEAR_ROUND -> gameService.clearRound(gameId)
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

    private fun getNameFromMessage(message: WSActionRequest): String? {
        return message.body?.get("playerName")?.asText()
    }

    private fun sendMessageToUser(gameId: String, userId: String, content: WSActionResponse.Content) {
        simpMessagingTemplate.convertAndSendToUser(userId, "/queue/game/${gameId}", content)
    }

    private fun sendMessageToAll(gameId: String, content: WSActionResponse.Content) {
        simpMessagingTemplate.convertAndSend("/topic/game/$gameId", content)
    }
}
