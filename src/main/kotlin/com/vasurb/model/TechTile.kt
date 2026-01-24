package com.vasurb.model

import com.vasurb.model.Card.Source.BLOODLINES
import com.vasurb.model.Card.Type.TECH
import com.vasurb.util.CardsUrlRetriever

data class TechTile(
    override val url: String,
    override val source: Card.Source = BLOODLINES
): Card {
    override val type: Card.Type = TECH

    var flipped: Boolean = false

    class All {
        fun get(): ArrayList<TechTile> {
            val tiles = CardsUrlRetriever.getImageUrls(TECH, BLOODLINES)
                .map { url -> TechTile(url) }
                .toMutableList() as ArrayList<TechTile>

            return ArrayList(tiles)
        }
    }
}
