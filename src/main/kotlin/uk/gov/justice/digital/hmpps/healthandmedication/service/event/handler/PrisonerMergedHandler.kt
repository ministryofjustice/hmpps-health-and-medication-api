package uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerHealthService
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.HmppsDomainEvent

@Transactional
@Service
class PrisonerMergedHandler(private val prisonerHealthService: PrisonerHealthService) {
  fun handle(personMerged: HmppsDomainEvent) {
    // Do nothing for now
  }
}
