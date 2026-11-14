package wombat.joshattic.us.ui.utils

import io.ktor.client.plugins.ResponseException

fun Throwable.friendlyMessage(
    default: String,
    unauthorized: String? = null,
    banned: Boolean = false,
    bannedMessage: String = "You can't do that while banned.",
): String = when (this) {
    is ResponseException -> when (response.status.value) {
        401 -> unauthorized ?: "You need to be signed in for that."
        403 -> if (banned) bannedMessage else "You need to log in again."
        else -> message?.let { "$default: $it" } ?: default
    }
    else -> message ?: default
}
