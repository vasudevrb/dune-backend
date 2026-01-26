package com.vasurb.model

import com.vasurb.model.CharacterSource.BLOODLINES
import com.vasurb.model.CharacterSource.IMPERIUM
import com.vasurb.model.CharacterSource.UPRISING

enum class PlayableCharacter(
    val readableName: String,
    val characterSource: CharacterSource = UPRISING
) {
    MARGOT_FENRING("Margot Fenring"),
    FEYD_RAUTHA("Feyd Rautha"),
    AMBER_METULLI("Amber Metulli"),
    LADY_JESSICA("Lady Jessica"),
    MUAD_DIB("Muad'Dib"),
    GURNEY_HALLECK("Gurney Halleck"),
    EMPEROR_SHADDAM("Shaddam Corrino"),
    PRINCESS_IRULAN("Princess Irulan"),
    STABAN_TUEK("Staban Tuek"),
    ARIANA_THORVALD("Ariana Thorvald", IMPERIUM),
    ARMAND_ECAZ("Armend Ecaz", IMPERIUM),
    GLOSSU_RABBAN("Glossu Rabban", IMPERIUM),
    HASIMIR_FENRING("Hasimir Fenring", BLOODLINES),
    HELEN_MOHIAM("Helen Mohiam", CharacterSource.OUT),
    CHANI("Chani", CharacterSource.OUT),
    DUNCAN_IDAHO("Duncan Idaho", CharacterSource.OUT),
    ESMAR_TUEK("Esmar Tuek", CharacterSource.OUT),
    KOTA_ODAX("Kota Odax", CharacterSource.OUT),
    STEERSMAN_YRKOON("Steersman Y'rkoon", BLOODLINES),
    PITER_DE_VRIES("Piter De Vries", BLOODLINES),
    HELENA_RICHESE("Helena Richese", IMPERIUM),
    ILBAN_RICHESE("Ilban Richese", IMPERIUM),
    LETO_ATREIDES("Leto Atreides", IMPERIUM),
    LIET_KYNES("Liet Kynes", BLOODLINES),
    MEMNON_THORVALD("Memnon Thorvald", IMPERIUM),
    PAUL_ATREIDES("Paul Atreides", IMPERIUM),
    VLADIMIR_HARKONNEN("Vladimir Harkonnen", IMPERIUM),
    YUNA_MORITANI("Yuna Moritani", IMPERIUM)
}

enum class CharacterSource {
    UPRISING, IMPERIUM, BLOODLINES, OUT
}
