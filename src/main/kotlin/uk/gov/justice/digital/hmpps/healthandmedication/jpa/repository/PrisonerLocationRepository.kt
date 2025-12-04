package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerLocation

@Repository
interface PrisonerLocationRepository : JpaRepository<PrisonerLocation, String> {
  fun findByPrisonerNumber(prisonerNumber: String): PrisonerLocation?
}
