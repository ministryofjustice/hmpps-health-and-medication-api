package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.HousingLevelDto
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

  fun getLatestLocationData(prisonerNumber: String): PrisonerLocation? {
    val housingLocation = prisonApiClient.getHousingLocation(prisonerNumber)
    val prisonerSearchInfo = prisonerSearchClient.getPrisoner(prisonerNumber)
    val topLocationLevel = housingLocation?.levels?.find { it.level == 1 }

    if (housingLocation == null && prisonerSearchInfo == null) {
      return null
    }

    return PrisonerLocation(
      prisonerNumber = prisonerNumber,
      prisonId = prisonerSearchInfo?.prisonId,
      topLevelCode = topLocationLevel?.code,
      topLevelDescription = topLocationLevel?.description,
      location = buildLocationString(housingLocation?.levels),
      lastAdmissionDate = prisonerSearchInfo?.lastAdmissionDate,
    )
  }

  @Transactional
  fun updateLocationDataToLatest(prisonerNumber: String) {
    if (prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(prisonerNumber)).isNotEmpty()) {
      getLatestLocationData(prisonerNumber)?.let { prisonerLocationRepository.save(it) }
    }
  }

  private fun buildLocationString(levels: List<HousingLevelDto>?): String? = levels?.sortedBy { it.level }
    ?.joinToString("-") { it.code }
    ?.takeIf { it.isNotBlank() }
}
