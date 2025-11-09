package com.vasurb.model

data class Deck<T: Card>(
    val cards: ArrayList<T>,
    val reshuffleFrom: Deck<T>? = null,
) {

    init {
        cards.shuffle()
    }

    fun draw(): T {
        if (cards.isEmpty()) {
            cards.addAll(reshuffleFrom?.cards?.shuffled() ?: arrayListOf())
        }

        return cards.removeAt(0)
    }

    fun draw(num: Int): ArrayList<T> {
        return (0 until num).map { draw() }.toMutableList() as ArrayList<T>
    }
}
