package com.vasurb.util

import com.vasurb.model.PlayableCharacter
import com.vasurb.model.PlayableCharacter.AMBER_METULLI
import com.vasurb.model.PlayableCharacter.EMPEROR_SHADDAM
import com.vasurb.model.PlayableCharacter.FEYD_RAUTHA
import com.vasurb.model.PlayableCharacter.GURNEY_HALLECK
import com.vasurb.model.PlayableCharacter.LADY_JESSICA
import com.vasurb.model.PlayableCharacter.MARGOT_FENRING
import com.vasurb.model.PlayableCharacter.MUAD_DIB
import com.vasurb.model.PlayableCharacter.PRINCESS_IRULAN
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Component
class CharacterUrlRetriever (@Value("\${server_url}") val serverUrl: String) {

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
