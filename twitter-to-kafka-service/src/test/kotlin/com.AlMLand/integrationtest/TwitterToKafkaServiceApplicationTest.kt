import com.AlMLand.TwitterToKafkaServiceApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [TwitterToKafkaServiceApplication::class])
class TwitterToKafkaServiceApplicationTest {
    @Test
    fun `load context`() {
        println("succress")
    }
}