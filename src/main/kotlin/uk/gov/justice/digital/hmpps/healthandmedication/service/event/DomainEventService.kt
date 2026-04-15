package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.healthandmedication.config.EventProperties

@Service
class DomainEventService(
  private val eventProperties: EventProperties,
  private val domainEventsPublisher: DomainEventsPublisher,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  fun publish(event: PrisonerHealthUpdatedEvent) {
    if (!eventProperties.publish) return

    log.info("Publishing domain event: ${event.eventType} for prisoner ${event.prisonerNumber}")

    domainEventsPublisher.publish(
      HmppsDomainEvent(
        eventType = event.eventType,
        detailUrl = "${eventProperties.baseUrl}/prisoners/${event.prisonerNumber}",
        occurredAt = event.occurredAt,
        description = "A prisoner had their health and medical information created or updated.",
        additionalInformation = HmppsAdditionalInformation(mutableMapOf("nomsNumber" to event.prisonerNumber)),
        personReference = PersonReference.withPrisonNumber(event.prisonerNumber),
      ),
    )
  }
}
