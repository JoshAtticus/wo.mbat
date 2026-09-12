package wombat.joshattic.us.data.network

import com.google.gson.Gson
import com.google.gson.JsonElement
import wombat.joshattic.us.data.model.Poster
import wombat.joshattic.us.data.model.Post

/**
 * Decoder for SvelteKit's devalue serialization, which the Alpha API returns
 * instead of human-readable JSON. In this format the `data` array holds a flat
 * pool of values where objects map keys to *indices* into the same array,
 * e.g. `{"poster":2,"content":6}` means "the value at index 2 / index 6".
 */
object DevalueParser {
    private val gson = Gson()

    /**
     * Parses a `posts/(id)/reposts/__data.json` response and returns the list
     * of reposts as regular [Post] objects. The root of the payload is
     * `{"post": <original post>, "reposts": [...]}`.
     */
    fun parseReposts(json: String): List<Post> {
        val root = runCatching { gson.fromJson(json, JsonElement::class.java) }.getOrNull()
            ?: return emptyList()
        val nodes = root.asJsonObject.getAsJsonArray("nodes") ?: return emptyList()
        val dataNode = nodes
            .firstOrNull { it.isJsonObject && it.asJsonObject.get("type")?.asString == "data" }
            ?.asJsonObject ?: return emptyList()
        val pool = dataNode.getAsJsonArray("data") ?: return emptyList()

        val memo = HashMap<Int, Any?>()
        fun resolve(index: Int): Any? {
            if (memo.containsKey(index)) return memo[index]
            if (index < 0 || index >= pool.size()) return null
            val element = pool[index]
            val value: Any? = when {
                element.isJsonNull -> null
                element.isJsonPrimitive -> {
                    val primitive = element.asJsonPrimitive
                    when {
                        primitive.isBoolean -> primitive.asBoolean
                        primitive.isNumber -> primitive.asNumber
                        else -> primitive.asString
                    }
                }
                // Devalue arrays are lists of indices into the pool — the
                // element *values* are the indices, not the array positions
                element.isJsonArray -> element.asJsonArray.map { ref ->
                    if (ref.isJsonPrimitive && ref.asJsonPrimitive.isNumber) resolve(ref.asInt) else null
                }
                element.isJsonObject -> element.asJsonObject.entrySet().associate { (key, ref) ->
                    key to if (ref.isJsonPrimitive && ref.asJsonPrimitive.isNumber) resolve(ref.asInt) else null
                }
                else -> null
            }
            memo[index] = value
            return value
        }

        for (i in 0 until pool.size()) resolve(i)

        val rootObject = memo[0] as? Map<*, *> ?: return emptyList()
        val reposts = rootObject["reposts"] as? List<*> ?: return emptyList()
        return reposts.mapNotNull { it.toPost() }
    }

    /** Alpha payloads key the id field as `id` instead of the regular API's `_id`. */
    private fun Any?.toPost(): Post? {
        val map = this as? Map<*, *> ?: return null
        val id = (map["id"] ?: map["_id"]) as? String ?: return null
        val posterMap = map["poster"] as? Map<*, *> ?: return null
        return Post(
            id = id,
            poster = Poster(
                id = posterMap["id"] as? String ?: "",
                name = posterMap["name"] as? String ?: "",
                color = posterMap["color"] as? String ?: ""
            ),
            content = map["content"] as? String ?: "",
            repost = map["repost"].toPost(),
            time = (map["time"] as? Number)?.toLong() ?: 0L,
            comments = (map["comments"] as? Number)?.toInt() ?: 0,
            loves = (map["loves"] as? Number)?.toInt() ?: 0,
            reposts = (map["reposts"] as? Number)?.toInt() ?: 0
        )
    }
}
