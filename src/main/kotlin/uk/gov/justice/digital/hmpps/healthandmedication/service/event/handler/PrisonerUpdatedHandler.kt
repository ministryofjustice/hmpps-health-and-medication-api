package uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerLocationService
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.nomsNumber

@Transactional
@Service
class PrisonerUpdatedHandler(private val prisonerLocationService: PrisonerLocationService) {
  fun handle(event: HmppsDomainEvent) {
    // The prisoner.received and prisoner.released events handled here both originate from Prisoner Search, so the
    // location information will definitely be indexed there and we don't need to call Prison API.
    prisonerLocationService.updateLocationDataToLatest(
      event.additionalInformation.nomsNumber,
      usePrisonerSearchOnly = true,
    )
  }
}
