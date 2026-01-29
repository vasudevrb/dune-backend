package com.vasurb.util

import com.vasurb.model.Card
import com.vasurb.model.Card.Source.*
import com.vasurb.model.Card.Type.*
import com.vasurb.model.ConflictCard.ConflictType
import com.vasurb.model.ReserveCard
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class CardsUrlRetriever(@Value("\${server_url}") val serverUrl: String) : CommandLineRunner {

     data class Key(
        val type: Card.Type,
        val source: Card.Source,
    )

    data class ReserveKey(
        val source: Card.Source,
        val reserveType: ReserveCard.ReserveType
    )

    data class ConflictKey(
        val source: Card.Source,
        val level: ConflictType
    )

    override fun run(vararg args: String?) {
        val resolver = PathMatchingResourcePatternResolver()

        Card.Type.entries
            .map { cardType ->
                if (cardType == CONFLICT) {
                    ConflictType.entries
                        .map { conflictLevel ->
                            cardImageUrls[ConflictKey(UPRISING, conflictLevel)] = getUrls(getConflictDirectory(UPRISING, conflictLevel), resolver)
                            cardImageUrls[ConflictKey(BLOODLINES, conflictLevel)] = getUrls(getConflictDirectory(BLOODLINES, conflictLevel), resolver)
                        }

                } else if (cardType == RESERVE) {
                    ReserveCard.ReserveType.entries
                        .map {reserveType ->
                            cardImageUrls[ReserveKey(UPRISING, reserveType)] = getUrls(getReserveDirectory(reserveType), resolver)
                        }
                } else {
                    Card.Source.entries
                        .map { source -> cardImageUrls[Key(cardType, source)] = getUrls(getDirectory(cardType, source), resolver) }
                }
            }

        print(cardImageUrls)
    }

    fun getUrls(directory: String, resolver: PathMatchingResourcePatternResolver): List<String> {
        val classPathDir = "classpath:/static/${directory}/*"
        return resolver.getResources(classPathDir)
            .filter { it.exists() && !it.file.isDirectory }
            .map { "${serverUrl}/${directory}/${it.filename}" }
    }

    fun getDirectory(cardType: Card.Type, source: Card.Source): String {
        return when (cardType) {
            IMPERIUM if source == BLOODLINES -> "imperium_cards/bloodlines"
            IMPERIUM -> "imperium_cards"
            INTRIGUE if source == BLOODLINES -> "intrigue_cards/bloodlines"
            INTRIGUE -> "intrigue_cards"
            CONTRACT if source == BLOODLINES -> "contracts/bloodlines"
            CONTRACT -> "contracts"
            STARTER -> "imperium_cards_starter"
            HAGAL -> "hagal_cards"
            TECH -> "tech_tiles"
            SARDAUKAR_SKILL -> "commander_skills"
            NAVIGATION -> "navigation_cards"
            TWISTED_INTRIGUE -> "twisted_intrigue_cards"
            else -> "no_op"
        }
    }

    fun getConflictDirectory(source: Card.Source, conflictType: ConflictType): String {
        return when (conflictType) {
            ConflictType.LEVEL_1 if source == BLOODLINES -> "conflict_cards/bloodlines/level_1"
            ConflictType.LEVEL_1 -> "conflict_cards/level_1"
            ConflictType.LEVEL_2 if source == BLOODLINES -> "conflict_cards/bloodlines/level_2"
            ConflictType.LEVEL_2 -> "conflict_cards/level_2"
            ConflictType.LEVEL_3 -> "conflict_cards/level_3"
        }
    }

    fun getReserveDirectory(reserveType: ReserveCard.ReserveType): String {
        return when (reserveType) {
            ReserveCard.ReserveType.THE_SPICE_MUST_FLOW -> "reserve_cards/the_spice_must_flow"
            ReserveCard.ReserveType.PREPARE_THE_WAY -> "reserve_cards/prepare_the_way"
        }
    }

    companion object {
        val cardImageUrls = mutableMapOf<Any, List<String>>()

        fun getImageUrls(cardType: Card.Type, source: Card.Source): List<String> {
            if (cardType == CONFLICT) {
                throw RuntimeException("conflict card type is not supported.")
            }

            return cardImageUrls[Key(cardType, source)] ?: emptyList()
        }

        fun getConflictImageUrls(source: Card.Source, level: ConflictType): List<String> {
            return cardImageUrls[ConflictKey(source, level)] ?: emptyList()
        }

        fun getReserveImageUrls(type: ReserveCard.ReserveType): List<String> {
            return cardImageUrls[ReserveKey(UPRISING, type)] ?: emptyList()
        }
    }
}
