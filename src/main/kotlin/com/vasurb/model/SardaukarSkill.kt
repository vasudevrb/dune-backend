package com.vasurb.model

import com.vasurb.util.CardsUrlRetriever

data class SardaukarSkill(
    override val url: String
): Card {
    override val type: Card.Type = Card.Type.SARDAUKAR_SKILL

    class All {
        fun get(): ArrayList<SardaukarSkill> {
            val skills = CardsUrlRetriever.cardImageUrls[CardsUrlRetriever.Key(Card.Type.SARDAUKAR_SKILL)]
                ?.map { url -> SardaukarSkill(url) }
                ?.toMutableList() as ArrayList<SardaukarSkill>

            return ArrayList(skills)
        }
    }
}
