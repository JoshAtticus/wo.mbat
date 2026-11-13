package wombat.joshattic.us.data

object LoveCache {
    private val loved = mutableMapOf<String, Boolean>()

    fun get(postId: String): Boolean? = loved[postId]

    fun put(postId: String, isLoved: Boolean) {
        loved[postId] = isLoved
    }
}
