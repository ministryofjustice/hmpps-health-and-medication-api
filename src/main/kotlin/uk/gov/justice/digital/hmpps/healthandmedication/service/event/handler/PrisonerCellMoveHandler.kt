package uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerLocationService
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.nomsNumber

@Transactional
@Service
class PrisonerCellMoveHandler(private val prisonerLocationService: PrisonerLocationService) {
  fun handle(event: HmppsDomainEvent) {
    // We cannot rely on Prisoner Search to get the location for the cell.move event, as the new location may not yet be
    // indexed at the time of processing this event, so we fetch it from Prison API in this instance.
    prisonerLocationService.updateLocationDataToLatest(
      event.additionalInformation.nomsNumber,
      usePrisonerSearchOnly = false,
    )
  }
}
