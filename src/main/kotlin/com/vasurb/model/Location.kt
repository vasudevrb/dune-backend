package com.vasurb.model


data class Location(
    val name: String,
    val id: Int
) {
    var agents: ArrayList<Agent> = arrayListOf()
    var spies: ArrayList<Spy> = arrayListOf()

    data class Agent(val agentId: String, val color: String, val playerName: String)
    data class Spy(val spyId: String, val color: String, val playerName: String)

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
            Location(locationName, 10)
        }
    }
}
