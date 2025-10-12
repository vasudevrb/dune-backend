package com.vasurb.model

data class Location(
    var name: String
) {
    var agents: ArrayList<Agent> = arrayListOf()
    var spies: ArrayList<Spy> = arrayListOf()
}
