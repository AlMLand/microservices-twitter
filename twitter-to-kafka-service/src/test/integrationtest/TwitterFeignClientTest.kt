import com.AlMLand.TwitterToKafkaServiceApplication
import com.AlMLand.config.TwitterProperties
import com.AlMLand.feign.controller.TwitterFeignClient
import org.junit.jupiter.api.Assertions.assertTrue
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
class TwitterFeignClientTest @Autowired constructor(
    private val twitterFeignClient: TwitterFeignClient, private val twitterProperties: TwitterProperties
) {
    @Test
    fun `getTweet, should return live one tweet, show what for object is returned`() {
        val response = twitterFeignClient.getTweet(twitterProperties.twitterV2BearerToken)

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