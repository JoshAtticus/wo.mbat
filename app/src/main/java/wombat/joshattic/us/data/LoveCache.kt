package wombat.joshattic.us.data

object LoveCache {
    private val loved = mutableMapOf<String, Boolean>()

    @Synchronized
    fun get(postId: String): Boolean? = loved[postId]

    @Synchronized
    fun put(postId: String, isLoved: Boolean) {
        loved[postId] = isLoved
    }
}
