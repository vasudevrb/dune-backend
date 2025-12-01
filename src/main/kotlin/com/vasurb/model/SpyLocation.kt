package com.vasurb.model

data class SpyLocation(
    val id: Int,
    val locationIds: List<Int>
) {
    var spies = arrayListOf<Spy>()

    data class Spy(val spyId: String, val color: String, val playerName: String)

    class All {
        fun get(): List<SpyLocation> = listOf(
            SpyLocation(1, listOf(1, 2)),
            SpyLocation(2, listOf(3, 4)),
            SpyLocation(3, listOf(5, 6)),
            SpyLocation(4, listOf(7, 8)),
            SpyLocation(5, listOf(9)),
            SpyLocation(6, listOf(10)),
            SpyLocation(7, listOf(11)),
            SpyLocation(8, listOf(12, 13)),
            SpyLocation(9, listOf(13, 14)),
            SpyLocation(10, listOf(14, 15)),
            SpyLocation(11, listOf(16, 18, 19)),
            SpyLocation(12, listOf(17, 20)),
            SpyLocation(13, listOf(21, 22)),
        )
    }
}
