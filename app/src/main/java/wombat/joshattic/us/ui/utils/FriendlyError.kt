package wombat.joshattic.us.ui.utils

import retrofit2.HttpException

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
