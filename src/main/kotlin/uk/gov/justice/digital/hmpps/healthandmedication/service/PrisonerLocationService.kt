package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerLocation
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.PrisonerHealthRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.PrisonerLocationRepository

@Service
@Transactional(readOnly = true)
class PrisonerLocationService(
  @Qualifier("systemPrisonApiClient") private val prisonApiClient: PrisonApiClient,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerLocationRepository: PrisonerLocationRepository,
  private val prisonerHealthRepository: PrisonerHealthRepository,
) {

  fun getLatestLocationData(prisonerNumber: String, usePrisonerSearchOnly: Boolean): PrisonerLocation? {
    val prisonerSearchInfo = prisonerSearchClient.getPrisoner(prisonerNumber) ?: return null

    val (prisonId, location) = if (usePrisonerSearchOnly) {
      Pair(prisonerSearchInfo.prisonId, prisonerSearchInfo.cellLocation)
    } else {
      val offender = prisonApiClient.getOffender(prisonerNumber)
      Pair(offender?.assignedLivingUnit?.agencyId, offender?.assignedLivingUnit?.description)
    }

    return PrisonerLocation(
      prisonerNumber = prisonerNumber,
      prisonId = prisonId,
      topLocationLevel = getTopLevelLocation(location),
      location = location,
      lastAdmissionDate = prisonerSearchInfo.lastAdmissionDate,
    )
  }

  @Transactional
  fun updateLocationDataToLatest(prisonerNumber: String, usePrisonerSearchOnly: Boolean) {
    if (prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(prisonerNumber)).isNotEmpty()) {
      getLatestLocationData(prisonerNumber, usePrisonerSearchOnly)?.let { prisonerLocationRepository.save(it) }
    }
  }

  private fun getTopLevelLocation(location: String?): String? = location?.split("-")?.first()
}
