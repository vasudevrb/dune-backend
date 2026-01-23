package com.vasurb.model

import com.vasurb.util.CardsUrlRetriever

data class TechTile(
    override val url: String
): Card {
    override val type: Card.Type = Card.Type.TECH

    class All {
        fun get(): ArrayList<TechTile> {
            val tiles = CardsUrlRetriever.cardImageUrls[CardsUrlRetriever.Key(Card.Type.TECH)]
                ?.map { url -> TechTile(url) }
                ?.toMutableList() as ArrayList<TechTile>

            return ArrayList(tiles)
        }
    }
}
