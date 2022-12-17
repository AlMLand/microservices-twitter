import com.AlMLand.TwitterToKafkaServiceApplication
import com.AlMLand.config.TwitterProperties
import com.AlMLand.feign.controller.TwitterClient
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
class TwitterClientTest @Autowired constructor(
    private val twitterClient: TwitterClient, private val twitterProperties: TwitterProperties
) {
    @Test
    fun `getTweet, should return live one tweet, show what for object is returned`() {
        val response = twitterClient.getTweet(twitterProperties.twitterV2BearerToken)

        val expectedKeys = listOf("data", "includes", "matching_rules")
        response.keys.forEach { assertTrue(expectedKeys.contains(it)) }

        val expectedKeyWords = listOf("data", "created_at", "id", "text", "user")
        val responseAsString = response.toString()
        for (keyWord in expectedKeyWords) {
            assertTrue(responseAsString.contains(keyWord))
        }
    }
}