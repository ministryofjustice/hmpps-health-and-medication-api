package uk.gov.justice.digital.hmpps.healthandmedication.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PrisonApiExtension
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PrisonApiExtension.Companion.prisonApi
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PrisonerSearchExtension
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PrisonerSearchExtension.Companion.prisonerSearch
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.expectNoFieldHistoryFor
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.HmppsDomainEvent
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.MissingTopicException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import uk.gov.justice.hmpps.sqs.publish
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class, PrisonApiExtension::class, PrisonerSearchExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @MockitoSpyBean
  lateinit var hmppsQueueService: HmppsQueueService

  @BeforeEach
  fun `clear queues`() {
    hmppsDomainEventsQueue.sqsClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(hmppsDomainEventsQueue.queueUrl).build(),
    ).get()
  }

  val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("hmppseventtopic")
      ?: throw MissingTopicException("hmppseventtopic not found")
  }

  internal val hmppsDomainEventsQueue by lazy {
    hmppsQueueService.findByQueueId("hmppsdomaineventsqueue")
      ?: throw MissingQueueException("hmppsdomaineventsqueue queue not found")
  }

  internal fun sendDomainEvent(event: HmppsDomainEvent) {
    domainEventsTopic.publish(event.eventType, objectMapper.writeValueAsString(event))
  }

  internal fun HmppsQueue.countAllMessagesOnQueue() = sqsClient.countAllMessagesOnQueue(queueUrl).get()

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun <T> expectFieldHistory(field: HealthAndMedicationField, vararg comparison: HistoryComparison<T>) = expectFieldHistory(field, fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER), *comparison)

  protected fun expectNoFieldHistoryFor(vararg field: HealthAndMedicationField) {
    val history = fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER)
    field.forEach { expectNoFieldHistoryFor(it, history) }
  }

  protected fun expectFieldMetadata(prisonerNumber: String, vararg comparison: FieldMetadata) {
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(prisonerNumber)).containsExactlyInAnyOrder(*comparison)
  }

  protected fun expectFieldMetadata(vararg comparison: FieldMetadata) = expectFieldMetadata(PRISONER_NUMBER, *comparison)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
    prisonApi.stubHealthPing(status)
    prisonerSearch.stubHealthPing(status)
  }
}
