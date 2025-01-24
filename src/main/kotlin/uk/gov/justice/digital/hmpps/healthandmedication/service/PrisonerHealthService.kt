package uk.gov.justice.digital.hmpps.healthandmedication.service

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.healthandmedication.dto.request.PrisonerHealthUpdateRequest
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
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
    prisonerHealthRepository.findById(prisonerNumber).getOrNull()?.toDto()

  fun getHealthForPrison(
    prisonId: String,
    request: HealthAndMedicationForPrisonRequest,
  ): HealthAndMedicationForPrisonResponse? {
    val prisoners = when (request.sort.isEmpty()) {
      true -> prisonerSearchClient.getPrisonersForPrison(prisonId)
      false -> {
        val sort = request.sort.split(",")
        if (sort[0] == "prisonerName") {
          prisonerSearchClient.getPrisonersForPrison(prisonId, "firstName,lastName," + sort[1])
        } else if (sort[0] == "location") {
          prisonerSearchClient.getPrisonersForPrison(prisonId, "cellLocation," + sort[1])
        } else {
          throw ValidationException("400 BAD_REQUEST Validation failure: Sort field invalid, please provide one of ${request.validSortFields()}")
        }
      }
    }

    if (!prisoners.isNullOrEmpty()) {
      val prisonerNumbers = prisoners.map { it.prisonerNumber }.toMutableList()
      val healthData =
        prisonerHealthRepository.findAllByPrisonerNumberInAndFoodAllergiesIsNotEmptyOrMedicalDietaryRequirementsIsNotEmpty(
          prisonerNumbers,
          request.pageable(),
        )

      // This maintains the order from the prisoner search API
      val overlappingIds = prisonerNumbers.intersect(healthData.map { it.prisonerNumber })

      return HealthAndMedicationForPrisonResponse(
        content = overlappingIds.map { id ->
          val health = healthData.find { it.prisonerNumber == id }!!
          val prisoner = prisoners.find { prisoner -> prisoner.prisonerNumber == health.prisonerNumber }!!
          HealthAndMedicationForPrisonDto(
            firstName = prisoner.firstName,
            lastName = prisoner.lastName,
            location = prisoner.cellLocation,
            prisonerNumber = health.prisonerNumber,
            health = health.toDto(),
          )
        }.toList(),
        metadata = PageMeta(
          first = healthData.isFirst,
          last = healthData.isLast,
          numberOfElements = healthData.numberOfElements,
          offset = healthData.pageable.offset.toInt(),
          pageNumber = healthData.pageable.pageNumber,
          size = healthData.size,
          totalElements = healthData.totalElements.toInt(),
          totalPages = healthData.totalPages,
        ),
      )
    }

    return null
  }

  @Transactional
  fun createOrUpdate(
    prisonerNumber: String,
    request: PrisonerHealthUpdateRequest,
  ): HealthDto {
    val now = ZonedDateTime.now(clock)
    val health = prisonerHealthRepository.findById(prisonerNumber).orElseGet { newHealthFor(prisonerNumber) }.apply {
      request.foodAllergies.let<List<String>> {
        foodAllergies.apply { clear() }.addAll(
          it!!.map { allergyCode ->
            FoodAllergy(
              prisonerNumber = prisonerNumber,
              allergy = toReferenceDataCode(referenceDataCodeRepository, allergyCode)!!,
            )
          },
        )
      }

      request.medicalDietaryRequirements.let<List<String>> {
        medicalDietaryRequirements.apply { clear() }.addAll(
          it!!.map { dietaryCode ->
            MedicalDietaryRequirement(
              prisonerNumber = prisonerNumber,
              dietaryRequirement = toReferenceDataCode(referenceDataCodeRepository, dietaryCode)!!,
            )
          },
        )
      }
    }.also { it.updateFieldHistory(now, authenticationFacade.getUserOrSystemInContext()) }

    return prisonerHealthRepository.save(health).toDto()
  }

  private fun newHealthFor(prisonerNumber: String): PrisonerHealth {
    validatePrisonerNumber(prisonerSearchClient, prisonerNumber)
    return PrisonerHealth(prisonerNumber)
  }
}
