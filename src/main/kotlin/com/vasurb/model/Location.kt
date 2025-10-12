package com.vasurb.model

import com.vasurb.model.Track.Track.TRACKS

data class Location(
    val name: String,
    val tracks: List<Track>
) {
    var agents: ArrayList<Agent> = arrayListOf()

    companion object Location {
        val LOCATIONS = listOf(
            "Fremkit",
            "Desert Tactics",
            "Secrets",
            "Espionage",
            "Deliver Supplies",
            "Heighliner",
            "Dutiful Service",
            "Sardaukar",
            "High Council",
            "Imperial Privilege",
            "Swordmaster",
            "Gather Support",
            "Assembly Hall",
            "Assembly Hall",
            "Shipping",
            "Accept Contract",
            "Sietch Tabr",
            "Research Station",
            "Spice Refinery",
            "Arrakeen",
            "Deep Desert",
            "Hagga Basin",
            "Imperial Basin",
        ).map { locationName ->
            Location(
                locationName,
                TRACKS.filter { track -> track.name.contains(locationName) }
            )
        }
    }
}
