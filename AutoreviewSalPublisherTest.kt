package devai.modules.autoreview.queue.publisher

import devai.modules.autoreview.queue.model.AutoreviewSalPayload
import devai.modules.autoreview.queue.publisher.exception.MessagePublishingException
import devai.modules.shared.config.JacksonConfig
import devai.modules.shared.features.DevAiCoreFeatureService
import devai.modules.shared.messaging.SQSMessagePublisher
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import readTestFile

@ExtendWith(MockKExtension::class)
class AutoreviewSalPublisherTest {
    private val objectMapper = JacksonConfig().objectMapper()

    val mockFeatureService = mockk<DevAiCoreFeatureService>()
    val sqsMessagePublisher = mockk<SQSMessagePublisher>(relaxed = true)

    @BeforeEach
    fun beforeEach() {
        every { mockFeatureService.isSpringSQSQueuePublishingEnabled() } returns true
    }

    @SpyK
    var subject =
        AutoreviewSalPublisher(
            sqsMessagePublisher,
            mockFeatureService,
            "bogus-queue-name",
        )

    @Test
    fun `should not throw exception on success`() {
        coEvery { sqsMessagePublisher.send(any<String>(), any<String>(), any()) } returns true

        val autoreviewSalPayload: AutoreviewSalPayload =
            objectMapper.readValue(
                readTestFile("sal/autoreview-sal-create-payload.json"),
                AutoreviewSalPayload::class.java,
            )

        assertDoesNotThrow {
            subject.publishMessage(autoreviewSalPayload)
        }
    }

    @Test
    fun `should throw exception on failure`() {
        coEvery { sqsMessagePublisher.send(any<String>(), any<String>(), any()) } returns false

        val autoreviewSalPayload: AutoreviewSalPayload =
            objectMapper.readValue(
                readTestFile("sal/autoreview-sal-create-payload.json"),
                AutoreviewSalPayload::class.java,
            )

        assertThrows<MessagePublishingException> {
            subject.publishMessage(autoreviewSalPayload)
        }
    }
}
