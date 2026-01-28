package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler.PrisonerCellMoveHandler
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler.PrisonerMergedHandler
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler.PrisonerUpdatedHandler

@Service
@ConditionalOnProperty(name = ["hmpps.sqs.enabled"], havingValue = "true")
class DomainEventsListener(
  private val jsonMapper: JsonMapper,
  private val prisonerUpdatedHandler: PrisonerUpdatedHandler,
  private val prisonerMergedHandler: PrisonerMergedHandler,
  private val prisonerCellMoveHandler: PrisonerCellMoveHandler,
) {
  init {
    log.info("Created SQS Domain Events Listener")
  }

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun receive(notification: Notification) {
    val event = jsonMapper.readValue<HmppsDomainEvent>(notification.message)
    when (notification.eventType) {
      PRISONER_RECEIVED, PRISONER_RELEASED -> prisonerUpdatedHandler.handle(event)
      PRISONER_CELL_MOVE -> prisonerCellMoveHandler.handle(event)
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
