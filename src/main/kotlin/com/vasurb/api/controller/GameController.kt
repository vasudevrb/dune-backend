package com.vasurb.api.controller

import com.vasurb.api.model.Character
import com.vasurb.api.model.RequestCharactersBody
import com.vasurb.api.model.CharactersResponse
import com.vasurb.api.model.CreateGameResponse
import com.vasurb.api.model.JoinGameResponse
import com.vasurb.api.model.JoinGameResponse.JoinGameState.CANNOT_JOIN_GAME_STARTED
import com.vasurb.api.model.JoinGameResponse.JoinGameState.CANNOT_JOIN_MAX_PLAYERS
import com.vasurb.api.model.JoinGameResponse.JoinGameState.IN_GAME
import com.vasurb.api.model.JoinGameResponse.JoinGameState.IN_LOBBY
import com.vasurb.api.model.PickCharacterBody
import com.vasurb.api.model.PickCharacterResponse
import com.vasurb.api.model.PickRivalBody
import com.vasurb.model.CharacterModel
import com.vasurb.model.Color
import com.vasurb.model.Player
import com.vasurb.service.GameService
import com.vasurb.util.Util
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController(val gameService: GameService) {

    @GetMapping("/create-game")
    fun createGame(
        @RequestParam playerName: String,
        @RequestParam includeRivals: Boolean = false,
        @RequestParam includeBloodlines: Boolean = false,
        @RequestParam includeAtomics: Boolean = false
    ): CreateGameResponse {
        val game = gameService.createGame(playerName, includeRivals, includeBloodlines, includeAtomics)
        return CreateGameResponse(game.gameId, gameService.getTurnOrder(game.gameId, playerName))
    }

    @GetMapping("/join-game")
    fun joinGame(@RequestParam playerName: String, @RequestParam gameId: String): JoinGameResponse {
        val game = gameService.getGame(gameId)
        val player = game.getPlayerByName(playerName)
        val joinState = when {
            player != null && player.character != null && !game.isStarted -> IN_LOBBY
            player != null && player.character != null && game.isStarted -> IN_GAME
            player != null -> JoinGameResponse.JoinGameState.PREVIOUSLY_JOINED
            game.isStarted -> CANNOT_JOIN_GAME_STARTED
            game.players.size >= 4 -> CANNOT_JOIN_MAX_PLAYERS
            else -> {
                gameService.addPlayer(playerName, gameId).gameId
                JoinGameResponse.JoinGameState.JOINED
            }
        }
        return JoinGameResponse(gameId, joinState, gameService.getTurnOrder(gameId, playerName))
    }

    @PostMapping("/characters")
    fun characters(@RequestBody body: RequestCharactersBody): CharactersResponse {
        val characters = gameService.getPresentableCharacters(body.playerName, body.gameId)
        return CharactersResponse(characters.map { Character(it.name, CharacterModel.get(it).urls, CharacterModel.get(it).avatarUrl) })
    }

    @PostMapping("/rivals")
    fun rivals(): CharactersResponse {
        val characters = gameService.getRivals()
        return CharactersResponse(characters.map { Character(it.name, CharacterModel.get(it).urls, CharacterModel.get(it).avatarUrl) })
    }

    @PostMapping("/pick-character")
    fun pickCharacter(@RequestBody body: PickCharacterBody): PickCharacterResponse {
        val player = gameService.getPlayer(body.gameId, body.playerName)
        player.character = CharacterModel.get(body.characterName)

        gameService.updatePlayer(player, body.gameId)
        return PickCharacterResponse(player.name)
    }

    @PostMapping("/pick-rival")
    fun pickRival(@RequestBody body: PickRivalBody): PickCharacterResponse {
        val playerName = "Rival ${body.rivalName.readableName}"
        gameService.addPlayer(
            playerName,
            body.gameId,
            isHost = false,
            isRival = true
        )

        val player = gameService.getPlayer(body.gameId, playerName)
        player.character = CharacterModel.get(body.rivalName)

        gameService.updatePlayer(player, body.gameId)
        return PickCharacterResponse(player.name)
    }

    companion object {
        const val NUM_PICKABLE_CHARACTERS = 1
    }
}
