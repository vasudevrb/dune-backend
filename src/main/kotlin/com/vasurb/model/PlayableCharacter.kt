package com.vasurb.model

import com.vasurb.model.CharacterSource.BLOODLINES
import com.vasurb.model.CharacterSource.CONSPIRACY
import com.vasurb.model.CharacterSource.OUT
import com.vasurb.model.CharacterSource.UPRISING

enum class PlayableCharacter(
    val readableName: String,
    val tier: Tier,
    val characterSource: CharacterSource = UPRISING,
) {
  STABAN_TUEK("Staban Tuek", Tier.A),
  ESMAR_TUEK("Esmar Tuek", Tier.A, BLOODLINES),
  HASIMIR_FENRING("Hasimir Fenring", Tier.A, BLOODLINES),
  KOTA_ODAX("Kota Odax", Tier.A, BLOODLINES),
  LIET_KYNES("Liet Kynes", Tier.A, BLOODLINES),
  ARIANA_THORVALD("Ariana Thorvald", Tier.A, BLOODLINES),
  MEMNON_THORVALD("Memnon Thorvald", Tier.A, BLOODLINES),

  STEERSMAN_YRKOON("Steersman Y'rkoon", Tier.B, BLOODLINES),
  CHANI("Chani", Tier.B, BLOODLINES),
  DUNCAN_IDAHO("Duncan Idaho", Tier.B, BLOODLINES),
  HELEN_MOHIAM("Helen Mohiam", Tier.B, BLOODLINES),
  PRINCESS_IRULAN("Princess Irulan", Tier.B),
  AMBER_METULLI("Amber Metulli", Tier.B),
  GURNEY_HALLECK("Gurney Halleck", Tier.B),
  PAUL_ATREIDES("Paul Atreides", Tier.B, BLOODLINES),
  ILBAN_RICHESE("Ilban Richese", Tier.B, BLOODLINES),
  PITER_DE_VRIES("Piter De Vries", Tier.B, BLOODLINES),

  VLADIMIR_HARKONNEN("Vladimir Harkonnen", Tier.C, BLOODLINES),
  FEYD_RAUTHA("Feyd Rautha", Tier.C),
  EMPEROR_SHADDAM("Shaddam Corrino", Tier.C),
  MARGOT_FENRING("Margot Fenring", Tier.C),
  LADY_JESSICA("Lady Jessica", Tier.C),
  YUNA_MORITANI("Yuna Moritani", Tier.C, BLOODLINES),
  LETO_ATREIDES("Leto Atreides", Tier.C, BLOODLINES),
  MUAD_DIB("Muad'Dib", Tier.C),

  SCYTALE("Scytale", Tier.A, CONSPIRACY),
  HAYT("Hayt", Tier.A, CONSPIRACY),
  COUNT_GORLU_SYNOCHE("Count Gorlu Synoche", Tier.A, CONSPIRACY),
  ALIA_ATREIDES("Alia Atreides", Tier.A, CONSPIRACY),

  ARMAND_ECAZ("Armend Ecaz", Tier.C, OUT),
  HELENA_RICHESE("Helena Richese", Tier.C, OUT),
  GLOSSU_RABBAN("Glossu Rabban", Tier.A, OUT),
}

enum class CharacterSource {
    UPRISING, IMPERIUM, BLOODLINES, CONSPIRACY, OUT
}

enum class Tier {
  A, B, C
}
