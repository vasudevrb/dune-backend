package com.vasurb.model

import com.vasurb.model.Card.Source
import com.vasurb.model.Card.Source.UPRISING
import com.vasurb.model.ConflictCard.ConflictType.*
import com.vasurb.util.CardsUrlRetriever
import com.vasurb.util.CardsUrlRetriever.Key

data class ConflictCard(
    override val url: String,
    override val source: Source,
    val conflictType: ConflictType,
): Card {
    override val type: Card.Type = Card.Type.CONFLICT

    enum class ConflictType(val level: Int) {
        LEVEL_1(1), LEVEL_2(2), LEVEL_3(3)
    }

    class All {
        fun getConflicts(allowedSources: List<Source> = listOf(UPRISING)): ArrayList<ConflictCard>{
            val level1 = Source.entries
                .filter { allowedSources.contains(it) }
                .flatMap { source ->
                    CardsUrlRetriever.getConflictImageUrls(source, LEVEL_1)
                        .map { url -> ConflictCard(url, source, LEVEL_1) }
                }
                .toMutableList() as ArrayList<ConflictCard>

            val level2 = Source.entries
                .filter { allowedSources.contains(it) }
                .flatMap { source ->
                    CardsUrlRetriever.getConflictImageUrls(source, LEVEL_2)
                        .map { url -> ConflictCard(url, source, LEVEL_2) }
                }
                .toMutableList() as ArrayList<ConflictCard>

            val level3 = Source.entries
                .filter { allowedSources.contains(it) }
                .flatMap { source ->
                    CardsUrlRetriever.getConflictImageUrls(source, LEVEL_3)
                        .map { url -> ConflictCard(url, source, LEVEL_3) }
                }
                .toMutableList() as ArrayList<ConflictCard>

            return level1.take(1)
                .plus(level2.take(4))
                .plus(level3)
                .toMutableList() as ArrayList<ConflictCard>
        }

        fun get(): ArrayList<ImperiumCard> {
            val cards = Source.entries
                .flatMap { source ->
                    CardsUrlRetriever.getImageUrls(Card.Type.IMPERIUM, source)
                        .map { url -> ImperiumCard(url, source) }
                }
                .toMutableList() as ArrayList<ImperiumCard>

            return ArrayList(cards)
        }

    }
}
