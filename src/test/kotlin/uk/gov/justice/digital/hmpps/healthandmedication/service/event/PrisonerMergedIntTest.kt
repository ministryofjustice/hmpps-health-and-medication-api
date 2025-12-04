package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.RepopulateDb
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.DomainEventsListener.Companion.PRISONER_MERGED
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.PersonReference.Companion.withPrisonNumber
import java.time.Duration.ofSeconds
import java.time.ZonedDateTime

class PrisonerMergedIntTest : IntegrationTestBase() {

  companion object {
    private const val PRISONER_NUMBER_NOT_FOUND = "Z9999ZZ"
  }

  @Disabled("Merge handling not yet implemented")
  @Test
  fun `message ignored if prison number not of interest`() {
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER_NOT_FOUND))).isEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isEmpty()
    assertThat(fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isNull()

    sendDomainEvent(personMergedEvent(PRISONER_NUMBER_NOT_FOUND, PRISONER_NUMBER_NOT_FOUND))

    await withPollDelay ofSeconds(1) untilCallTo { hmppsDomainEventsQueue.countAllMessagesOnQueue() } matches { it == 0 }
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER_NOT_FOUND))).isEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isEmpty()
    assertThat(fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isNull()
  }

  @Disabled("Merge handling not yet implemented")
  @Test
  @Transactional
  @RepopulateDb
  fun `merge event causes health data to be deleted for the old prisoner number`() {
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).isNotEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).isNotEmpty()
    assertThat(fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).isNotEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER)).isNotNull()
    sendDomainEvent(personMergedEvent(PRISONER_NUMBER, PRISONER_NUMBER))

    await withPollDelay ofSeconds(1) untilCallTo { hmppsDomainEventsQueue.countAllMessagesOnQueue() } matches { it == 0 }
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).isEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).isEmpty()
    assertThat(fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).isEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER)).isNull()
  }

  private fun personMergedEvent(
    prisonNumber: String,
    removedPrisonNumber: String,
    occurredAt: ZonedDateTime = ZonedDateTime.now(),
    eventType: String = PRISONER_MERGED,
    detailUrl: String? = null,
    description: String = "A prisoner was merged",
  ) = HmppsDomainEvent(
    eventType,
    1,
    detailUrl,
    occurredAt,
    description,
    HmppsAdditionalInformation(mutableMapOf("nomsNumber" to prisonNumber, "removedNomsNumber" to removedPrisonNumber)),
    withPrisonNumber(prisonNumber),
  )
}
