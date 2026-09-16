package wombat.joshattic.us.ui.utils

import retrofit2.HttpException

/**
 * Maps raw HTTP status codes to user-facing text so error codes like
 * "HTTP 401" never leak into the UI.
 *
 * A 403 usually means banned, but it can also mean an unauthenticated/expired
 * session — callers pass [banned] only when the local profile is known to be
 * banned; otherwise the user is told to sign in again.
 */
fun Throwable.friendlyMessage(
    default: String,
    unauthorized: String? = null,
    banned: Boolean = false,
    bannedMessage: String = "You can't do that while banned.",
): String = when (this) {
    is HttpException -> when (code()) {
        401 -> unauthorized ?: "You need to be signed in for that."
        403 -> if (banned) bannedMessage else "You need to log in again."
        else -> localizedMessage?.let { "$default: $it" } ?: default
    }
    else -> localizedMessage ?: default
}
