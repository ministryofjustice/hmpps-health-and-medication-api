package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.healthandmedication.config.EventProperties
import java.time.ZonedDateTime

class DomainEventServiceTest {

  private val domainEventsPublisher = mock<DomainEventsPublisher>()
  private val baseUrl = "http://localhost:8080"

  @Test
  fun `publishes event with correct shape`() {
    val domainEventService = DomainEventService(EventProperties(publish = true, baseUrl = baseUrl), domainEventsPublisher)

    domainEventService.publish(EVENT)

    val captor = argumentCaptor<HmppsDomainEvent>()
    verify(domainEventsPublisher).publish(captor.capture())
    assertThat(captor.firstValue).isEqualTo(
      HmppsDomainEvent(
        eventType = EVENT.eventType,
        detailUrl = "$baseUrl/prisoners/$PRISONER_NUMBER",
        occurredAt = EVENT.occurredAt,
        description = "A prisoner had their dietary information created or updated.",
        additionalInformation = HmppsAdditionalInformation(mutableMapOf("nomsNumber" to PRISONER_NUMBER)),
        personReference = PersonReference.withPrisonNumber(PRISONER_NUMBER),
      ),
    )
  }

  @Test
  fun `does not publish event when publish is disabled`() {
    val domainEventService = DomainEventService(EventProperties(publish = false, baseUrl = baseUrl), domainEventsPublisher)

    domainEventService.publish(EVENT)

    verify(domainEventsPublisher, never()).publish(any())
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    val NOW: ZonedDateTime = ZonedDateTime.now()
    val EVENT = PrisonerHealthUpdatedEvent(
      eventType = DomainEventsPublisher.PRISONER_DIETARY_INFORMATION_UPDATED,
      prisonerNumber = PRISONER_NUMBER,
      occurredAt = NOW,
    )
  }
}
