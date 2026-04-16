package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.RepopulateDb
import java.time.Duration.ofSeconds

class DietAndAllergyEventsIntTest : IntegrationTestBase() {

  @Nested
  inner class NewData {

    @Test
    @Sql("classpath:jpa/repository/reset.sql")
    fun `an event is published when new diet and allergy data is created`() {
      putDietAndAllergyData(ALL_FIELDS_REQUEST)

      val events = awaitEvents(1)
      assertThat(events.single().eventType).isEqualTo(DomainEventsPublisher.PRISONER_DIETARY_INFORMATION_UPDATED)
    }
  }

  @Nested
  inner class ExistingData {

    @Test
    @RepopulateDb
    fun `no event is published when data is unchanged`() {
      putDietAndAllergyData(UNCHANGED_REQUEST)
      awaitNoEvents()
    }

    @Test
    @RepopulateDb
    fun `an event is published when data changes`() {
      putDietAndAllergyData(ALL_FIELDS_REQUEST)
      val events = awaitEvents(1)
      assertThat(events.single().eventType).isEqualTo(DomainEventsPublisher.PRISONER_DIETARY_INFORMATION_UPDATED)
    }
  }

  private fun putDietAndAllergyData(requestBody: String) {
    webTestClient.put().uri("/prisoners/$PRISONER_NUMBER/diet-and-allergy")
      .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW")))
      .header("Content-Type", "application/json")
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isOk
  }

  private fun awaitEvents(expectedCount: Int): List<HmppsDomainEvent> {
    await untilCallTo { hmppsEventTestQueue.countAllMessagesOnQueue() } matches { it == expectedCount }
    return receiveEvents()
  }

  private fun awaitNoEvents() {
    await withPollDelay ofSeconds(1) untilCallTo {
      hmppsEventTestQueue.countAllMessagesOnQueue()
    } matches { it == 0 }
  }

  private fun receiveEvents(): List<HmppsDomainEvent> {
    val response = hmppsEventTestQueue.sqsClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(hmppsEventTestQueue.queueUrl)
        .maxNumberOfMessages(10)
        .build(),
    ).get()
    return response.messages().map { msg ->
      val notification = jsonMapper.readValue<Notification>(msg.body())
      jsonMapper.readValue<HmppsDomainEvent>(notification.message)
    }
  }

  private companion object {
    // language=json
    val ALL_FIELDS_REQUEST = """
      {
        "foodAllergies": [{ "value": "FOOD_ALLERGY_MILK" }],
        "medicalDietaryRequirements": [{ "value": "MEDICAL_DIET_OTHER", "comment": "Some other diet" }],
        "personalisedDietaryRequirements": [{ "value": "PERSONALISED_DIET_HALAL" }],
        "cateringInstructions": "Some new catering instructions."
      }
    """.trimIndent()

    // Matches the state set up by @RepopulateDb + catering_instructions.sql exactly
    // language=json
    val UNCHANGED_REQUEST = """
      {
        "foodAllergies": [{ "value": "FOOD_ALLERGY_SOYA", "comment": "Allergic to soya." }],
        "medicalDietaryRequirements": [{ "value": "MEDICAL_DIET_LOW_CHOLESTEROL", "comment": "Requires low cholesterol diet." }],
        "personalisedDietaryRequirements": [{ "value": "PERSONALISED_DIET_KOSHER", "comment": "Requires kosher meals." }],
        "cateringInstructions": "Serve dessert before the main course."
      }
    """.trimIndent()
  }
}
