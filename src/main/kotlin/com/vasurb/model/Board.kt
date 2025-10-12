package com.vasurb.model

import com.vasurb.model.Location.Location.LOCATIONS

class Board(
    val locations: List<Location>
) {

    companion object Board {
        val INITIAL_STATE = Board(LOCATIONS)
    }
}
