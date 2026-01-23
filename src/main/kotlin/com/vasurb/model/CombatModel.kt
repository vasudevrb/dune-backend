package com.vasurb.model

data class CombatModel(
    var troopsInGarrison: Int = 3,
    var troopsInCombat: Int = 0,
    var commandersInGarrison: Int = 0,
    var commandersInCombat: Int = 0,
    var wormsInCombat: Int = 0,
    var strength: Int = 0
)
