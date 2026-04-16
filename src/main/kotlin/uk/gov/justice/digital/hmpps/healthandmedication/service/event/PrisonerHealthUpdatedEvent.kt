package uk.gov.justice.digital.hmpps.healthandmedication.service.event
import java.time.ZonedDateTime
data class PrisonerHealthUpdatedEvent(
  val eventType: String,
  val prisonerNumber: String,
  val occurredAt: ZonedDateTime,
)
