package com.vasurb.api.auth

import java.security.Principal

class PlayerPrincipal(val playerId: String) : Principal {
    override fun getName(): String = playerId
}
