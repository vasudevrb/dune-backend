package com.vasurb.model

import com.vasurb.model.Card.Source.BLOODLINES
import com.vasurb.model.Card.Type.NAVIGATION
import com.vasurb.util.CardsUrlRetriever

data class NavigationCard(
    override val url: String,
    override val source: Card.Source = BLOODLINES,
    var revealed: Boolean = false,
) : Card {
    override val type: Card.Type = NAVIGATION

    class All {
        fun get(): ArrayList<NavigationCard> {
            val navigationCards = CardsUrlRetriever.getImageUrls(NAVIGATION, BLOODLINES)
                .map { url -> NavigationCard(url) }
                .toMutableList() as ArrayList<NavigationCard>

            return ArrayList(navigationCards)
        }
    }
}

