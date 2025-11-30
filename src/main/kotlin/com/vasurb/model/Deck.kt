package com.vasurb.model

data class Deck<T: Card>(
    val cards: ArrayList<T>,
    val reshuffleFrom: ArrayList<T>? = null,
    val shuffleAtStart: Boolean = true
) {

    init {
        if (shuffleAtStart) cards.shuffle()
    }

    fun draw(): T {
        if (cards.isEmpty()) {
            cards.addAll(reshuffleFrom?.shuffled() ?: arrayListOf())
            reshuffleFrom?.clear()
        }

        return cards.removeAt(0)
    }

    fun peek(): T? {
        if (cards.isEmpty()) {
            return null
        }

        return cards[0]
    }

    fun draw(num: Int): ArrayList<T> {
        return (0 until num).map { draw() }.toMutableList() as ArrayList<T>
    }

    fun size(): Int = cards.size
}
