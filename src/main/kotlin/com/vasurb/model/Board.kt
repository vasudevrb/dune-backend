package com.vasurb.model

class Board(
    val locations: List<Location>
) {

    companion object Board {
        val INITIAL_STATE = Board(
            listOf(Location("Fremkit"))
        )
    }
}
