package wombat.joshattic.us.data.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.Poster

object DevalueParser {
    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun parseReposts(json: String): List<Post> {
        val root = runCatching { jsonParser.parseToJsonElement(json) }.getOrNull()
            ?: return emptyList()
        val nodes = (root as? JsonObject)?.get("nodes") as? JsonArray ?: return emptyList()
        val dataNode = nodes
            .firstOrNull { it is JsonObject && it["type"]?.jsonPrimitive?.contentOrNull == "data" }
            ?.jsonObject ?: return emptyList()
        val pool = dataNode["data"] as? JsonArray ?: return emptyList()

        val memo = HashMap<Int, Any?>()
        fun resolve(index: Int): Any? {
            if (memo.containsKey(index)) return memo[index]
            if (index < 0 || index >= pool.size) return null
            val element = pool[index]
            val value: Any? = when (element) {
                is JsonNull -> null
                is JsonPrimitive -> {
                    if (element.isString) {
                        element.content
                    } else {
                        element.booleanOrNull ?: element.doubleOrNull ?: element.contentOrNull
                    }
                }
                is JsonArray -> element.map { ref ->
                    val refIndex = (ref as? JsonPrimitive)?.intOrNull
                    if (refIndex != null) resolve(refIndex) else null
                }
                is JsonObject -> element.entries.associate { (key, ref) ->
                    val refIndex = (ref as? JsonPrimitive)?.intOrNull
                    key to if (refIndex != null) resolve(refIndex) else null
                }
            }
            memo[index] = value
            return value
        }

        for (i in 0 until pool.size) resolve(i)

        val rootObject = memo[0] as? Map<*, *> ?: return emptyList()
        val reposts = rootObject["reposts"] as? List<*> ?: return emptyList()
        return reposts.mapNotNull { it.toPost() }
    }

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
