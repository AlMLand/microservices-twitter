import com.AlMLand.TwitterToKafkaServiceApplication
import com.AlMLand.feign.controller.TwitterFeignClient
import com.AlMLand.twitterToKafkaService.TwitterProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(
    classes = [TwitterToKafkaServiceApplication::class],
    webEnvironment = RANDOM_PORT
)
@SuppressWarnings("MaxLineLength")
@Disabled // sometimes the test fails, simply because the tests before it delete the rules
class TwitterFeignClientTest @Autowired constructor(
    private val twitterFeignClient: TwitterFeignClient, private val twitterProperties: TwitterProperties,
    private val objectMapper: ObjectMapper
) {

    @Test
    fun `createRules`() {
        val rules = twitterFeignClient.getRules(twitterProperties.twitterBearerToken)
        val result = (rules["data"] as? ArrayList<LinkedHashMap<String, Any>>)
            ?.map { it["id"] }
            ?.joinToString(prefix = "\"", postfix = "\"", separator = "\",\"")
        twitterFeignClient.deleteRules(
            twitterProperties.twitterBearerToken,
            String.format("{\"delete\":{\"ids\":[%s]}}", result ?: "0")
        )

    }

    @Test
    fun `deleteRules`() {
        val rules = twitterFeignClient.getRules(twitterProperties.twitterBearerToken)
        val result = (rules["data"] as? ArrayList<LinkedHashMap<String, Any>>)
            ?.map { it["id"] }
            ?.joinToString(prefix = "\"", postfix = "\"", separator = "\",\"")

        twitterFeignClient.deleteRules(
            twitterProperties.twitterBearerToken,
            String.format("{\"delete\":{\"ids\":[%s]}}", result ?: "0")
        )
        val rulesAfterDeleting = twitterFeignClient.getRules(twitterProperties.twitterBearerToken)
        assertTrue(rulesAfterDeleting["data"] == null)
    }

    @Test
    fun `getRules - existing rules from twitter`() {
        val rules = twitterFeignClient.getRules(twitterProperties.twitterBearerToken)
        val json = objectMapper.writeValueAsString(rules)
        println("TEST: $json")
    }

    @Test
    fun `getTweet - should return live one tweet, show what for object is returned`() {
        val response = twitterFeignClient.getTweet(twitterProperties.twitterBearerToken)

        val expectedKeys = listOf("data", "includes", "matching_rules")
        response.keys.forEach { assertTrue(expectedKeys.contains(it)) }

        val expectedKeyWords = listOf("data", "created_at", "id", "text", "user")
        val responseAsString = response.toString()
        for (keyWord in expectedKeyWords) {
            assertTrue(responseAsString.contains(keyWord))
        }

//        (response["data"] as LinkedHashMap<*, *>).forEach { (k, v) -> println("KEY_TYPE: ${k.javaClass} | VALUE_TYPE: ${v.javaClass}") }
//        ((response["data"] as LinkedHashMap<*, *>)["edit_history_tweet_ids"] as ArrayList<*>).forEach { println("ARRAYLIST_ELEMENT_TYPE: ${it.javaClass}") }
//        (response["includes"] as LinkedHashMap<*, *>).forEach { (k, v) -> println("KEY_TYPE: ${k.javaClass} | VALUE_TYPE: ${v.javaClass}") }
//        ((response["includes"] as LinkedHashMap<*, *>)["users"] as ArrayList<*>).forEach { println("KEY_TYPE: ${it.javaClass}") }
//        (((response["includes"] as LinkedHashMap<*, *>)["users"] as ArrayList<*>)[0] as LinkedHashMap<*, *>).forEach { (k, v) -> println("KEY_TYPE: ${k.javaClass} | VALUE_TYPE: ${v.javaClass}")}
//        (response["matching_rules"] as ArrayList<*>).forEach { k -> println("KEY_TYPE: ${k.javaClass}") }
//        ((response["matching_rules"] as ArrayList<*>)[0] as LinkedHashMap<*, *>).forEach { k, v -> println("KEY_TYPE: ${k.javaClass} | VALUE_TYPE: ${v.javaClass}") }
    }
}