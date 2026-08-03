package com.vasurb.util

import com.vasurb.model.PlayableCharacter
import com.vasurb.model.PlayableCharacter.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
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
            STABAN_TUEK -> listOf("staban_tuek")
            ARIANA_THORVALD -> listOf("ariana_thorvald")
            ARMAND_ECAZ -> listOf("armand_ecaz")
            GLOSSU_RABBAN -> listOf("glossu_rabban")
            HASIMIR_FENRING -> listOf("hasimir_fenring")
            HELEN_MOHIAM -> listOf("helen_mohiam")
            CHANI -> listOf("chani")
            DUNCAN_IDAHO -> listOf("duncan_idaho")
            KOTA_ODAX -> listOf("kota_odax")
            STEERSMAN_YRKOON -> listOf("steersman_yrkoon")
            PITER_DE_VRIES -> listOf("piter_de_vries")
            ESMAR_TUEK -> listOf("esmar_tuek")
            HELENA_RICHESE -> listOf("helena_richese")
            ILBAN_RICHESE -> listOf("ilban_richese")
            LETO_ATREIDES -> listOf("leto_atreides")
            LIET_KYNES -> listOf("liet_kynes")
            MEMNON_THORVALD -> listOf("memnon_thorvald")
            PAUL_ATREIDES -> listOf("paul_atreides")
            VLADIMIR_HARKONNEN -> listOf("vladimir_harkonnen")
            YUNA_MORITANI -> listOf("yuna_moritani")
            ALIA_ATREIDES -> listOf("alia_atreides")
            HAYT -> listOf("hayt")
            SCYTALE -> listOf("scytale")
            COUNT_GORLU_SYNOCHE -> listOf("count_gorlu_synoche")
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
            STABAN_TUEK -> ("staban_tuek")
            ARIANA_THORVALD -> ("ariana_thorvald")
            ARMAND_ECAZ -> ("armand_ecaz")
            GLOSSU_RABBAN -> ("glossu_rabban")
            HASIMIR_FENRING -> ("hasimir_fenring")
            HELEN_MOHIAM -> ("helen_mohaim")
            CHANI -> ("chani")
            DUNCAN_IDAHO -> ("duncan_idaho")
            KOTA_ODAX -> ("kota_odax")
            STEERSMAN_YRKOON -> ("steersman_yrkoon")
            PITER_DE_VRIES -> ("piter_de_vries")
            ESMAR_TUEK -> ("esmar_tuek")
            HELENA_RICHESE -> ("helena_richese")
            ILBAN_RICHESE -> ("ilban_richese")
            LETO_ATREIDES -> ("leto_atreides")
            LIET_KYNES -> ("liet_kynes")
            MEMNON_THORVALD -> ("memnon_thorvald")
            PAUL_ATREIDES -> ("paul_atreides")
            VLADIMIR_HARKONNEN -> ("vladimir_harkonnen")
            YUNA_MORITANI -> ("yuna_moritani")
            ALIA_ATREIDES -> ("alia_atreides")
            HAYT -> ("hayt")
            SCYTALE -> ("scytale")
            COUNT_GORLU_SYNOCHE -> ("count_gorlu_synoche")
        }

        return serverUrl + "/avatars/${fileName}.jpg"
    }
}
