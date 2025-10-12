package com.vasurb.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidActionException(message: String): RuntimeException(message) {
}
