package wombat.joshattic.us

import org.junit.Test
import wombat.joshattic.us.data.network.DevalueParser

class DevalueParserTest {
    // Payload captured from a post with zero reposts (plus one variation with reposts).
    private val emptyRepostsPayload = """
        {"type":"data","nodes":[{"type":"skip"},{"type":"skip"},{"type":"data","data":[
        {"post":1,"reposts":27},
        {"poster":2,"content":6,"repost":7,"time":22,"revisions":23,"comments":25,"loves":19,"reposts":17,"loved":20,"id":26},
        {"name":3,"id":4,"color":5},"eris","64a0f2e9cc742649b441308b","violet",
        "<p>WE ARE CHARLIEEE</p>",
        {"poster":8,"content":12,"time":13,"revisions":14,"comments":17,"loves":18,"reposts":19,"loved":20,"id":21},
        {"name":9,"id":10,"color":11},"verity","6aa493f32f994596b28ae34c","indigo",
        "<p>anniversary</p>",
        1789176242435,[15],{"content":12,"time":13,"current":16},true,0,3,2,null,"6aa4a9b22f994596b28ae35b",
        1789178881251,[24],{"content":6,"time":22,"current":16},1,"6aa4b4012f994596b28ae35c",[]],
        "uses":{"params":["id"]}}]}
    """.trimIndent()

    @Test
    fun parsesEmptyRepostsPayload() {
        val result = DevalueParser.parseReposts(emptyRepostsPayload)
        assert(result.isEmpty()) { "expected empty but got ${result.size}" }
    }

    private val withRepostsPayload = """
        {"type":"data","nodes":[{"type":"skip"},{"type":"skip"},{"type":"data","data":[
        {"post":1,"reposts":16},{"poster":2,"content":6,"time":7,"revisions":8,"comments":11,"loves":12,"reposts":13,"loved":14,"id":15},
        {"name":3,"id":4,"color":5},"verity","6aa493f32f994596b28ae34c","indigo",
        "<p>original</p>",1789176242435,[9],{"content":6,"time":7,"current":10},true,0,3,2,null,"6aa4a9b22f994596b28ae35b",
        [17,31],
        {"poster":18,"content":21,"repost":22,"time":26,"revisions":27,"comments":29,"loves":29,"reposts":11,"loved":14,"id":30},
        {"name":19,"id":20,"color":5},"joshatticus","649a8a9dd989fb6e279167a5",
        "<p>quote</p>",{"poster":23,"content":6,"time":7,"revisions":24,"comments":11,"loves":12,"reposts":13,"loved":14,"id":15},
        {"name":3,"id":4,"color":5},[25],{"content":6,"time":7,"current":10},1789183013364,[28],
        {"content":21,"time":26,"current":10},1,"6aa4c4252f994596b28ae371",
        {"poster":32,"content":36,"repost":37,"time":41,"revisions":42,"comments":29,"loves":13,"reposts":11,"loved":14,"id":44},
        {"name":33,"id":34,"color":35},"eris","64a0f2e9cc742649b441308b","violet",
        "<p>WE ARE CHARLIEEE</p>",{"poster":38,"content":6,"time":7,"revisions":39,"comments":11,"loves":12,"reposts":13,"loved":14,"id":15},
        {"name":3,"id":4,"color":5},[40],{"content":6,"time":7,"current":10},1789178881251,[43],
        {"content":36,"time":41,"current":10},"6aa4b4012f994596b28ae35c"],
        "uses":{"params":["id"]}}]}
    """.trimIndent()

    @Test
    fun parsesRepostsPayload() {
        val result = DevalueParser.parseReposts(withRepostsPayload)
        assert(result.size == 2) { "expected 2 reposts but got ${result.size}" }
        val first = result[0]
        assert(first.id == "6aa4c4252f994596b28ae371") { "wrong id: ${first.id}" }
        assert(first.poster.name == "joshatticus") { "wrong poster: ${first.poster.name}" }
        assert(first.time == 1789183013364L) { "wrong time: ${first.time}" }
        assert(first.repost?.id == "6aa4a9b22f994596b28ae35b") { "wrong quoted post id: ${first.repost?.id}" }
        assert(result[1].poster.name == "eris") { "wrong second poster: ${result[1].poster.name}" }
    }
}
