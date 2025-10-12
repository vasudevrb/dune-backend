package com.vasurb.model

class Track(
    val name: String
) {
    var spies: List<Spy> = arrayListOf()

    companion object Track {
        val TRACKS = listOf(
            "Fremkit; Desert Tactics",
            "Secrets; Espionage",
            "Deliver Supplies; Heighliner",
            "Dutiful Service; Sardaukar",
            "High Council; Imperial Privilege; Swordmaster",
            "Assembly Hall; Gather Support",
            "Shipping; Accept Contract",
            "Sietch Tabr; Research Station",
            "Research Station; Spice Refinery",
            "Spice Refinery; Arrakeen",
            "Deep Desert",
            "Hagga Basin",
            "Imperial Basin",
        ).map { trackName -> Track(trackName) }
    }
}
