package wombat.joshattic.us.data

/**
 * Process-wide memory cache of known love states, so a post we already know
 * we loved can render as liked immediately on reload while the server
 * confirmation is still in flight.
 */
object LoveCache {
    private val loved = mutableMapOf<String, Boolean>()

    @Synchronized
    fun get(postId: String): Boolean? = loved[postId]

    @Synchronized
    fun put(postId: String, isLoved: Boolean) {
        loved[postId] = isLoved
    }
}
