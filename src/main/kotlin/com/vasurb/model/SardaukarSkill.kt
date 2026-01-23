package com.vasurb.model

import com.vasurb.model.Card.Source.BLOODLINES
import com.vasurb.model.Card.Type.SARDAUKAR_SKILL
import com.vasurb.util.CardsUrlRetriever

data class SardaukarSkill(
    override val url: String,
    override val source: Card.Source = BLOODLINES
): Card {
    override val type: Card.Type = SARDAUKAR_SKILL

    class All {
        fun get(): ArrayList<SardaukarSkill> {
            val skills = CardsUrlRetriever.getImageUrls(SARDAUKAR_SKILL, BLOODLINES)
                .map { url -> SardaukarSkill(url) }
                .toMutableList() as ArrayList<SardaukarSkill>

            return ArrayList(skills)
        }
    }
}
