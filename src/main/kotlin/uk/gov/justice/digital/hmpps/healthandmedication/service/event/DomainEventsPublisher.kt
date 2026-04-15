package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.publish

@Service
class DomainEventsPublisher(private val hmppsQueueService: HmppsQueueService, private val jsonMapper: JsonMapper) {
  private val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("hmppseventtopic") ?: throw IllegalStateException("hmppseventtopic not found")
  }

  fun publish(domainEvent: DomainEvent) {
    domainEventsTopic.publish(domainEvent.eventType, jsonMapper.writeValueAsString(domainEvent))
  }

  companion object {
    const val PRISONER_DIETARY_INFORMATION_UPDATED = "prison-offender-events.prisoner.dietary.information.updated"
  }
}