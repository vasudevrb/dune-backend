package com.vasurb.model

import com.vasurb.util.CharacterUrlRetriever
import com.vasurb.util.RivalUrlRetriever

data class CharacterModel(
    val name: String,
    val urls: List<String>,
    val avatarUrl: String,
    val additionalInfo: CharacterAdditionalInfo
) {

    data class CharacterAdditionalInfo(
        var signetStatus: Int? = 0,
        var duncanAgentDeployed: Agent? = null
    )

    companion object {
        val MUAD_DIB = get(PlayableCharacter.MUAD_DIB)

        fun get(c: PlayableCharacter): CharacterModel {
            return CharacterModel(
                c.readableName,
                CharacterUrlRetriever.characterImageUrls.getValue(c),
                CharacterUrlRetriever.avatarImageUrl.getValue(c),
                CharacterAdditionalInfo()
            )
        }

        fun get(c: RivalCharacter): CharacterModel {
            return CharacterModel(
                c.readableName,
                RivalUrlRetriever.rivalImageUrls.getValue(c),
                RivalUrlRetriever.avatarImageUrl.getValue(c),
                CharacterAdditionalInfo()
            )
        }
    }
}
