package com.vasurb.api.controller

import com.vasurb.api.model.Character
import com.vasurb.api.model.RequestCharactersBody
import com.vasurb.api.model.CharactersResponse
import com.vasurb.api.model.CreateGameResponse
import com.vasurb.api.model.JoinGameResponse
import com.vasurb.api.model.PickCharacterBody
import com.vasurb.api.model.PickCharacterResponse
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

@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
class GameController(val gameService: GameService) {

    @GetMapping("/create-game")
    fun createGame(@RequestParam playerName: String): CreateGameResponse {
        val hostPlayer = Player(
            playerName,
            isHost = true,
            color = Color.GOLD
        )

        return CreateGameResponse(gameService.createGame(hostPlayer).gameId)
    }

    @GetMapping("/join-game")
    fun joinGame(@RequestParam playerName: String, @RequestParam gameId: String): JoinGameResponse {
        val game = gameService.getGame(gameId)
        val color = game.availableColors.random()
        val player = Player(playerName, color = color)
        game.availableColors.remove(color)
        return JoinGameResponse(gameService.addPlayer(player, gameId).gameId)
    }

    @PostMapping("/characters")
    fun characters(@RequestBody body: RequestCharactersBody): CharactersResponse {
        val game = gameService.getGame(body.gameId)
        val characters = game.presentedCharacters[body.playerName] ?: game.availableCharacters
            .shuffled(Util.getDeterministicRandom(body.playerName))
            .take(NUM_PICKABLE_CHARACTERS)

        //TODO: Handle when characters is an empty list?
        characters.forEach { game.availableCharacters.remove(it) }
        game.presentedCharacters[body.playerName] = characters
        return CharactersResponse(characters.map { Character(it, CharacterModel.get(it).urls) })
    }

    @PostMapping("/pick-character")
    fun pickCharacter(@RequestBody body: PickCharacterBody): PickCharacterResponse {
        val game = gameService.getGame(body.gameId)
        val player = gameService.getPlayer(body.gameId, body.playerName)
        player.character = CharacterModel.get(body.characterName)

        game.availableColors.remove(player.color)

        gameService.updatePlayer(player, body.gameId)
        return PickCharacterResponse(player.name)
    }

    companion object {
        const val NUM_PICKABLE_CHARACTERS = 2
    }
}
