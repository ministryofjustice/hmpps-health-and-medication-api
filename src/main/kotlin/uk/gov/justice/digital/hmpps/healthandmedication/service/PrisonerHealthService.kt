package uk.gov.justice.digital.hmpps.healthandmedication.service

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatus
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatusUpdate
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.CateringInstructions
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.PrisonerHealthRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.HealthAndMedicationForPrisonRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.PageMeta
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.UpdateDietAndAllergyRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.UpdateSmokerStatusRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.DietAndAllergyResponse
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationForPrisonDto
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationForPrisonResponse
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationResponse
import uk.gov.justice.digital.hmpps.healthandmedication.utils.AuthenticationFacade
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Pagination
import uk.gov.justice.digital.hmpps.healthandmedication.utils.toReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.utils.validatePrisonerNumber
import java.time.Clock
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class PrisonerHealthService(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonApiClient: PrisonApiClient,
  private val prisonerHealthRepository: PrisonerHealthRepository,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val clock: Clock,
) {
  fun getHealth(prisonerNumber: String): HealthAndMedicationResponse? = prisonerHealthRepository.findById(prisonerNumber).getOrNull()?.toHealthDto()

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
      val healthData = prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(prisonerNumbers)

      // This maintains the order from the prisoner search API so that we're able to have sorting
      val healthForPrison =
        prisonerNumbers.intersect(healthData.map { it.prisonerNumber }.toSet()).toList().map { prisonerNumber ->
          val health = healthData.find { it.prisonerNumber == prisonerNumber }!!
          val prisoner = prisoners.find { prisoner -> prisoner.prisonerNumber == prisonerNumber }!!
          HealthAndMedicationForPrisonDto(
            firstName = prisoner.firstName,
            lastName = prisoner.lastName,
            location = prisoner.cellLocation,
            prisonerNumber = prisonerNumber,
            health = health.toHealthDto(),
          )
        }

      val (content, metadata) = Pagination.paginateCollection(request.page, request.size, healthForPrison)

      return HealthAndMedicationForPrisonResponse(
        content = content,
        // Pagination metadata, should be returned from the same class that returns the IDs calculated
        metadata = PageMeta(
          first = metadata.first,
          last = metadata.last,
          numberOfElements = metadata.numberOfElements,
          offset = metadata.offset,
          pageNumber = metadata.pageNumber,
          size = metadata.size,
          totalElements = metadata.totalElements,
          totalPages = metadata.totalPages,
        ),
      )
    }

    return null
  }

  @Transactional
  fun updateDietAndAllergyData(
    prisonerNumber: String,
    request: UpdateDietAndAllergyRequest,
  ): DietAndAllergyResponse {
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

      this.cateringInstructions = CateringInstructions(prisonerNumber, request.cateringInstructions)
    }.also { it.updateFieldHistory(now, authenticationFacade.getUserOrSystemInContext()) }

    return prisonerHealthRepository.save(health).toDietAndAllergyDto()
  }

  fun UpdateSmokerStatusRequest.convertToPrisonApiRequest(): PrisonApiSmokerStatusUpdate = PrisonApiSmokerStatusUpdate(
    when (this.smokerStatus) {
      "SMOKER_YES" -> PrisonApiSmokerStatus.Y
      "SMOKER_VAPER" -> PrisonApiSmokerStatus.V
      "SMOKER_NO" -> PrisonApiSmokerStatus.N
      else -> throw IllegalArgumentException("Invalid smoker status: $this")
    },
  )

  fun updateSmokerStatus(prisonerNumber: String, request: UpdateSmokerStatusRequest) {
    prisonApiClient.updateSmokerStatus(prisonerNumber, request.convertToPrisonApiRequest())
  }

  private fun newHealthFor(prisonerNumber: String): PrisonerHealth {
    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    return PrisonerHealth(prisonerNumber)
  }
}
