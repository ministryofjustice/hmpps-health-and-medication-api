package uk.gov.justice.digital.hmpps.healthandmedication.service

import jakarta.validation.ValidationException
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
import uk.gov.justice.digital.hmpps.healthandmedication.resource.requests.HealthAndMedicationForPrisonRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.requests.PageMeta
import uk.gov.justice.digital.hmpps.healthandmedication.resource.responses.HealthAndMedicationForPrisonDto
import uk.gov.justice.digital.hmpps.healthandmedication.resource.responses.HealthAndMedicationForPrisonResponse
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
  fun getHealth(prisonerNumber: String): HealthDto? =
    prisonerHealthRepository.findById(prisonerNumber).getOrNull()?.toHealthDto()

  fun getHealthForPrison(
    prisonId: String,
    request: HealthAndMedicationForPrisonRequest,
  ): HealthAndMedicationForPrisonResponse? {
    val prisoners = when (request.sort.isEmpty()) {
      true -> prisonerSearchClient.getPrisonersForPrison(prisonId)
      false -> {
        // This would be nice to move into request validation rather than having to validate here
        val sort = request.sort.split(",")
        if (sort[0] == "prisonerName") {
          prisonerSearchClient.getPrisonersForPrison(prisonId, "lastName,firstName," + sort[1])
        } else if (sort[0] == "location") {
          prisonerSearchClient.getPrisonersForPrison(prisonId, "cellLocation," + sort[1])
        } else {
          throw ValidationException("400 BAD_REQUEST Validation failure: Sort field invalid, please provide one of ${request.validSortFields()}")
        }
      }
    }

    if (!prisoners.isNullOrEmpty()) {
      val prisonerNumbers = prisoners.map { it.prisonerNumber }.toMutableList()

      // Fetch all non-empty health data for the given prisoner numbers
      val healthData =
        prisonerHealthRepository.findAllByPrisonerNumberInAndFoodAllergiesIsNotEmptyOrMedicalDietaryRequirementsIsNotEmpty(
          prisonerNumbers,
        )

      // This maintains the order from the prisoner search API so that we're able to have sorting
      val overlappingIds = prisonerNumbers.intersect(healthData.map { it.prisonerNumber }.toSet()).toList()

      // Pagination specific code to be moved out
      val startIndex = (request.page - 1) * request.size
      val lastIndex = (startIndex + request.size - 1).coerceAtMost(overlappingIds.size - 1)
      val idsForPage = overlappingIds.slice(startIndex..lastIndex)
      // End pagination specific code

      return HealthAndMedicationForPrisonResponse(
        content = idsForPage.map { id ->
          val health = healthData.find { it.prisonerNumber == id }!!
          val prisoner = prisoners.find { prisoner -> prisoner.prisonerNumber == health.prisonerNumber }!!
          HealthAndMedicationForPrisonDto(
            firstName = prisoner.firstName,
            lastName = prisoner.lastName,
            location = prisoner.cellLocation,
            prisonerNumber = health.prisonerNumber,
            health = health.toHealthDto(),
          )
        }.toList(),
        // Pagination metadata, should be returned from the same class that returns the IDs calculated
        metadata = PageMeta(
          first = startIndex == 0,
          last = (lastIndex + 1) >= overlappingIds.size,
          numberOfElements = idsForPage.size,
          offset = startIndex,
          pageNumber = if (idsForPage.isNotEmpty()) {
            Math.ceilDiv(startIndex, idsForPage.size) + 1
          } else {
            1
          },
          size = request.size,
          totalElements = overlappingIds.size,
          totalPages = Math.ceilDiv(overlappingIds.size, request.size).coerceAtLeast(1),
        ),
      )
    }

    return null
  }

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
