package com.vasurb.model


data class Location(
    val name: String,
    val id: Int
) {
    var agents: ArrayList<Agent> = arrayListOf()
    var controlFlag: ControlFlag? = null

    data class ControlFlag(val controlFlagId: String, val color: String, val playerName: String)
    data class Agent(val agentId: String, val color: String, val playerName: String)

    class All {
        fun get(): List<Location> = listOf(
            Location("Sardaukar", 1),
            Location("Dutiful Service", 2),
            Location("Heighliner", 3),
            Location("Deliver Supplies", 4),
            Location("Espionage", 5),
            Location("Secrets", 6),
            Location("Desert Tactics", 7),
            Location("Fremkit", 8),
            Location("Deep Desert", 9),
            Location("Hagga Basin", 10),
            Location("Imperial Basin", 11),
            Location("Sietch Tabr", 12),
            Location("Research Station", 13),
            Location("Spice Refinery", 14),
            Location("Arrakeen", 15),
            Location("High Council", 16),
            Location("Assembly Hall", 17),
            Location("Imperial Privilege", 18),
            Location("Swordmaster", 19),
            Location("Gather Support", 20),
            Location("Shipping", 21),
            Location("Accept Contract", 22),
        )
    }
}
