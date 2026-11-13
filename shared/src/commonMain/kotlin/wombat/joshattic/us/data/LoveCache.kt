package wombat.joshattic.us.data

import wombat.joshattic.us.platform.PlatformLock
import wombat.joshattic.us.platform.withLock

object LoveCache {
    private val lock = PlatformLock()
    private val loved = mutableMapOf<String, Boolean>()

    fun get(postId: String): Boolean? = lock.withLock { loved[postId] }

    fun put(postId: String, isLoved: Boolean) {
        lock.withLock {
            loved[postId] = isLoved
        }
    }
}
