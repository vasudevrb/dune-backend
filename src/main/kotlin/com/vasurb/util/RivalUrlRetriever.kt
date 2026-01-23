package com.vasurb.util

import com.vasurb.model.RivalCharacter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class RivalUrlRetriever(@Value("\${server_url}") val serverUrl: String) : CommandLineRunner {
    override fun run(vararg args: String?) {
        RivalCharacter.entries
            .map { rival ->
                rivalImageUrls[rival] = getUrls(rival)
                avatarImageUrl[rival] = getAvatarUrl(rival)
            }
    }

    companion object {
        val rivalImageUrls = mutableMapOf<RivalCharacter, List<String>>()
        val avatarImageUrl = mutableMapOf<RivalCharacter, String>()
    }

    fun getUrls(rival: RivalCharacter): List<String> {
        val fileNames: List<String> = when (rival) {
            RivalCharacter.MARGOT_FENRING -> listOf("margot_fenring")
            RivalCharacter.FEYD_RAUTHA -> listOf("feyd_rautha")
            RivalCharacter. LADY_JESSICA -> listOf("lady_jessica")
            RivalCharacter.MUAD_DIB -> listOf("muaddib")
            RivalCharacter.GURNEY_HALLECK -> listOf("gurney_halleck")
            RivalCharacter.PRINCESS_IRULAN -> listOf("princess_irulan")
            RivalCharacter.STABAN_TUEK -> listOf("staban_tuek")
            RivalCharacter.VLADIMIR_HARKONNEN -> listOf("vladimir_harkonnen")
        }

        return fileNames.map { name ->
            serverUrl + "/rival_characters/${name}.jpg"
        }
    }

    fun getAvatarUrl(character: RivalCharacter): String {
        val fileName: String = when (character) {
            RivalCharacter.MARGOT_FENRING -> ("margot_fenring")
            RivalCharacter.FEYD_RAUTHA -> ("feyd_rautha")
            RivalCharacter.LADY_JESSICA -> ("lady_jessica")
            RivalCharacter.MUAD_DIB -> ("muaddib")
            RivalCharacter.GURNEY_HALLECK -> ("gurney_halleck")
            RivalCharacter.PRINCESS_IRULAN -> ("princess_irulan")
            RivalCharacter.STABAN_TUEK -> ("staban_tuek")
            RivalCharacter.VLADIMIR_HARKONNEN -> ("vladimir_harkonnen")
        }

        return serverUrl + "/rival_avatars/${fileName}.jpg"
    }
}
