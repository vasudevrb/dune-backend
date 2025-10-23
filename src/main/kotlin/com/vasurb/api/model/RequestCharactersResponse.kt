package com.vasurb.api.model

import com.fasterxml.jackson.annotation.JsonValue
import com.vasurb.model.PlayableCharacter
import com.vasurb.model.PlayableCharacter.AMBER_METULLI
import com.vasurb.model.PlayableCharacter.EMPEROR_SHADDAM
import com.vasurb.model.PlayableCharacter.FEYD_RAUTHA
import com.vasurb.model.PlayableCharacter.GURNEY_HALLECK
import com.vasurb.model.PlayableCharacter.LADY_JESSICA
import com.vasurb.model.PlayableCharacter.MARGOT_FENRING
import com.vasurb.model.PlayableCharacter.MUAD_DIB
import com.vasurb.model.PlayableCharacter.PRINCESS_IRULAN
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

data class CharactersResponse(
    val characters: List<Character>
) {

    @JsonValue
    fun value() = characters
}

data class Character(
    val characterName: PlayableCharacter,
) {
    val urls: List<String> = getUrls(characterName)

    private fun getUrls(character: PlayableCharacter): List<String> {
        val fileNames: List<String> = when (character) {
            MARGOT_FENRING -> listOf("margot_fenring")
            FEYD_RAUTHA -> listOf("feyd_rautha")
            AMBER_METULLI -> listOf("amber_metulli")
            LADY_JESSICA -> listOf("lady_jessica_1", "lady_jessica_2")
            MUAD_DIB -> listOf("muaddib")
            GURNEY_HALLECK -> listOf("gurney_halleck")
            EMPEROR_SHADDAM -> listOf("shaddam_corrino")
            PRINCESS_IRULAN -> listOf("princess_irulan")
        }

        return fileNames.map { name ->
            ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/characters/${name}.jpg")
                .toUriString()
        }
    }
}
