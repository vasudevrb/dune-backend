package com.vasurb.api.model.ws_request

import com.vasurb.api.model.Action

data class WSActionRequest<T>(
    val action: Action.Type,
    val body: T
) {


}
