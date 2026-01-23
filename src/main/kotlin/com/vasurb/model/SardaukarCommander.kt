package com.vasurb.model

data class SardaukarCommander(
    val id: Int
) {
    var acquiredBy: String? = null

    class All {
        fun get(): ArrayList<SardaukarCommander>  = arrayListOf(
            SardaukarCommander(-1),
            SardaukarCommander(1),
            SardaukarCommander(2),
            SardaukarCommander(4),
            SardaukarCommander(16),
            SardaukarCommander(17),
            SardaukarCommander(20),
        )
    }
}
