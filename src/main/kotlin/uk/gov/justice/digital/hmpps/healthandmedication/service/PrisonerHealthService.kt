package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.healthandmedication.dto.request.UpdateDietAndAllergyRequest
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.DietAndAllergyDto
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.PrisonerHealthRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.utils.AuthenticationFacade
import uk.gov.justice.digital.hmpps.healthandmedication.utils.toReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.utils.validatePrisonerNumber
import java.time.Clock
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class PrisonerHealthService(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerHealthRepository: PrisonerHealthRepository,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val clock: Clock,
) {
  fun getHealth(prisonerNumber: String): HealthDto? = prisonerHealthRepository.findById(prisonerNumber).getOrNull()?.toHealthDto()

  @Transactional
  fun updateDietAndAllergyData(
    prisonerNumber: String,
    request: UpdateDietAndAllergyRequest,
  ): DietAndAllergyDto {
    val now = ZonedDateTime.now(clock)
    val health = prisonerHealthRepository.findById(prisonerNumber).orElseGet {
      newHealthFor(prisonerNumber)
    }.apply {
      foodAllergies.apply { clear() }.addAll(
        request.foodAllergies!!.map { allergy ->
          FoodAllergy(
            prisonerNumber = prisonerNumber,
            allergy = toReferenceDataCode(referenceDataCodeRepository, allergy.value)!!,
            commentText = allergy.comment,
          )
        },
      )

      medicalDietaryRequirements.apply { clear() }.addAll(
        request.medicalDietaryRequirements!!.map { diet ->
          MedicalDietaryRequirement(
            prisonerNumber = prisonerNumber,
            dietaryRequirement = toReferenceDataCode(referenceDataCodeRepository, diet.value)!!,
            commentText = diet.comment,
          )
        },
      )

      personalisedDietaryRequirements.apply { clear() }.addAll(
        request.personalisedDietaryRequirements!!.map { diet ->
          PersonalisedDietaryRequirement(
            prisonerNumber = prisonerNumber,
            dietaryRequirement = toReferenceDataCode(referenceDataCodeRepository, diet.value)!!,
            commentText = diet.comment,
          )
        },
      )
    }.also { it.updateFieldHistory(now, authenticationFacade.getUserOrSystemInContext()) }

    return prisonerHealthRepository.save(health).toDietAndAllergyDto()
  }

  private fun newHealthFor(prisonerNumber: String): PrisonerHealth {
    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    return PrisonerHealth(prisonerNumber)
  }
}
