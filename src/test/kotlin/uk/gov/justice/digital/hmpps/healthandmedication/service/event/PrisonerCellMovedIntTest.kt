package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerLocation
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.RepopulateDb
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.DomainEventsListener.Companion.PRISONER_CELL_MOVE
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.PersonReference.Companion.withPrisonNumber
import java.time.Duration.ofSeconds
import java.time.LocalDate
import java.time.ZonedDateTime

class PrisonerCellMovedIntTest : IntegrationTestBase() {

  companion object {
    private const val PRISONER_NUMBER_NOT_FOUND = "Z9999ZZ"
  }

  @Test
  fun `message ignored if prisoner does not have any existing health and medication data`() {
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER_NOT_FOUND))).isEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isNull()

    sendDomainEvent(
      domainEvent(
        prisonerNumber = PRISONER_NUMBER_NOT_FOUND,
        eventType = PRISONER_CELL_MOVE,
      ),
    )

    await withPollDelay ofSeconds(1) untilCallTo { hmppsDomainEventsQueue.countAllMessagesOnQueue() } matches { it == 0 }
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER_NOT_FOUND))).isEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isNull()
  }

  @Test
  @RepopulateDb
  fun `event causes location data to be updated`() {
    val before = PrisonerLocation(
      prisonerNumber = PRISONER_NUMBER,
      prisonId = "STI",
      topLocationLevel = "A",
      location = "A-1-001",
      lastAdmissionDate = LocalDate.parse("2025-11-21"),
    )
    val after = PrisonerLocation(
      prisonerNumber = PRISONER_NUMBER,
      prisonId = "MDI",
      topLocationLevel = "E",
      location = "E-9-011",
      lastAdmissionDate = LocalDate.parse("2025-11-28"),
    )

    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).isNotEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER)).isEqualTo(before)
    sendDomainEvent(
      domainEvent(
        prisonerNumber = PRISONER_NUMBER,
        eventType = PRISONER_CELL_MOVE,
      ),
    )

    await withPollDelay ofSeconds(1) untilCallTo { hmppsDomainEventsQueue.countAllMessagesOnQueue() } matches { it == 0 }
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER)).isEqualTo(after)
  }

  private fun domainEvent(
    prisonerNumber: String,
    eventType: String,
    occurredAt: ZonedDateTime = ZonedDateTime.now(),
    detailUrl: String? = null,
    description: String = "A prisoner was merged",
  ) = HmppsDomainEvent(
    eventType,
    1,
    detailUrl,
    occurredAt,
    description,
    HmppsAdditionalInformation(mutableMapOf("nomsNumber" to prisonerNumber)),
    withPrisonNumber(prisonerNumber),
  )
}
