package com.vasurb.model

import com.vasurb.util.CardsUrlRetriever
import com.vasurb.util.CardsUrlRetriever.Key

/**
 * 1
 * Smuggler's Harvester: 2
 * Unswerving Loyalty: 2
 * Weirding Woman: 2
 * Sardaukar Soldier: 1
 * Space-time Folding: 1
 *
 * 2
 * Maker Keeper: 2
 * Desert Survival: 2
 * Reliable Informant: 1
 * Imperial Spymaster: 1
 * Spy Network: 1
 * Hidden Missive: 1
 * Wheels Within Wheels: 1
 * Undercover Asset: 1
 * Fedaykin Stilltent: 1
 *
 * 3
 * Calculus of Power: 2
 * Double Agent: 2
 * Rebel Supplier: 2
 * Maula Pistol: 2
 * Bene Gesserit Operative: 2
 * Covert Operation: 1
 * Dangerous Rhetoric: 1
 * Branching Path: 1
 * Northern Watermaster: 1
 * Guild Spy: 1
 * Guild Envoy: 1
 * Ecological Testing Station: 1
 * Thumper: 1
 * The Beast's Spoils: 1
 *
 * 4
 * Public Spectacle: 2
 * Sardaukar Coordination: 2
 * Tread in Darkness: 2
 * Truthtrance: 2
 * Shishakli: 1
 * Smuggler's Haven: 1
 * Paracompass: 1
 * Southern Elders: 1
 *
 * 5
 * Spacing Guild's Favour: 2
 * Strike Flet: 1
 * Subversive Advisor: 1
 * Chani: Clever Tactician: 1
 * Treacherous Maneuver: 1
 * Leadership: 1
 * In High Places: 1
 * Captured Mentat: 1
 *
 * 6
 * Stilgar: 1
 * Desert Power: 1
 * Junction Headquarters: 1
 * Price is No Object: 1
 * Corrinth City: 1
 * Arrakis Revolt: 1
 *
 * 7
 * Long Live the Fighters: 1
 *
 * 8
 * Steersman: 1
 * Overthrow: 1
 */
data class ImperiumCard(
    override val fileName: String
) : AgentCard {

    override val type: Card.Type = Card.Type.IMPERIUM

    companion object {
        fun getAll(): ArrayList<AgentCard> {
            val cards = CardsUrlRetriever.cardImageUrls[Key(Card.Type.IMPERIUM)]
                ?.map { url -> ImperiumCard(url) }
                ?.toMutableList() as ArrayList<ImperiumCard>

            return ArrayList(cards)
        }
    }
}
