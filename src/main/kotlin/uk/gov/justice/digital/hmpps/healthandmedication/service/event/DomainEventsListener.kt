package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler.PrisonerMergedHandler
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler.PrisonerUpdatedHandler

@Service
@ConditionalOnProperty(name = ["hmpps.sqs.enabled"], havingValue = "true")
class DomainEventsListener(
  private val objectMapper: ObjectMapper,
  private val prisonerUpdatedHandler: PrisonerUpdatedHandler,
  private val prisonerMergedHandler: PrisonerMergedHandler,
) {
  init {
    log.info("Created SQS Domain Events Listener")
  }

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun receive(notification: Notification) {
    val event = objectMapper.readValue<HmppsDomainEvent>(notification.message)
    when (notification.eventType) {
      PRISONER_RECEIVED, PRISONER_RELEASED, PRISONER_CELL_MOVE -> prisonerUpdatedHandler.handle(event)
      PRISONER_MERGED -> prisonerMergedHandler.handle(event)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(DomainEventsListener::class.java)

    const val PRISONER_RECEIVED = "prisoner-offender-search.prisoner.received"
    const val PRISONER_RELEASED = "prisoner-offender-search.prisoner.released"
    const val PRISONER_MERGED = "prison-offender-events.prisoner.merged"
    const val PRISONER_CELL_MOVE = "prison-offender-events.prisoner.cell.move"
  }
}
