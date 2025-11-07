package com.vasurb.util

import com.vasurb.model.PlayableCharacter
import com.vasurb.model.PlayableCharacter.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class CharacterUrlRetriever(@Value("\${server_url}") val serverUrl: String) : CommandLineRunner {
    override fun run(vararg args: String?) {
        PlayableCharacter.entries
            .map { character ->
                characterImageUrls[character] = getUrls(character)
                avatarImageUrl[character] = getAvatarUrl(character)
            }

        println("Loaded character images: $characterImageUrls")
    }

    companion object {
        val characterImageUrls = mutableMapOf<PlayableCharacter, List<String>>()
        val avatarImageUrl = mutableMapOf<PlayableCharacter, String>()
    }

    fun getUrls(character: PlayableCharacter): List<String> {
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
            serverUrl + "/characters/${name}.jpg"
        }
    }

    fun getAvatarUrl(character: PlayableCharacter): String {
        val fileName: String = when (character) {
            MARGOT_FENRING -> ("margot_fenring")
            FEYD_RAUTHA -> ("feyd_rautha")
            AMBER_METULLI -> ("amber_metulli")
            LADY_JESSICA -> ("lady_jessica")
            MUAD_DIB -> ("muaddib")
            GURNEY_HALLECK -> ("gurney_halleck")
            EMPEROR_SHADDAM -> ("shaddam_corrino")
            PRINCESS_IRULAN -> ("princess_irulan")
        }

        return serverUrl + "/avatars/${fileName}.jpg"
    }
}
