package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergyHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergyHistoryItem
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementItem
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirementItem
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.SubjectAccessRequestFieldHistoryType
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.SubjectAccessRequestResponseDto
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.UNKNOWN_REFERENCE_DATA_DESCRIPTION
import uk.gov.justice.digital.hmpps.healthandmedication.utils.toReferenceDataCode
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.SortedSet

/*
  From https://dsdmoj.atlassian.net/wiki/spaces/SARS/pages/4780589084/Integration+Guide#Testing-Checklist
  - Does the endpoint respond with a 200 status code when hitting the health endpoint? YES
  - Does the endpoint respond with a 401 status code when the SAR endpoint is hit without a token? YES
  - Does the endpoint respond with a 403 status code when the SAR endpoint is hit with a token that does not have the ROLE_SAR_DATA_ACCESS role? YES
  - Does the endpoint respond with a 209 status code and empty response body when the SAR endpoint is hit with a valid token but
     with a subject identifier that this service does not use (eg. a CRN is provided but the service only contains data relating
     to PRNs)? YES
  - Does the endpoint respond with a 204 status code and empty response body when the SAR endpoint is hit with a valid token
     and a placeholder subject identifier that has no data? YES
  - Does the endpoint respond with a 200 status code when the SAR endpoint is hit with a valid token and valid subject identifier? YES
  - Does the endpoint respond with a response body in JSON format with a JSON object in the ‘content’ block? YES

  Please see SubjectAccessRequestResourceIntTest, which checks each above requirment.

  Useful URLs
    http://localhost:8080/subject-access-request?prn=G9154UN&fromDate=01/01/1980&toDate=01/03/2025
    http://localhost:8080/subject-access-request?prn=G5475UO&fromDate=01/01/1980&toDate=01/02/2025
    http://localhost:8080/subject-access-request?prn=G5475UO&fromDate=01/01/1980&toDate=23/02/2025
*/
@Service
class SubjectAccessRequestService(
  private val fieldHistoryRepository: FieldHistoryRepository,
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) : HmppsPrisonSubjectAccessRequestService {

  override fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent? {
    try {
      // Check Dates
      //  - Date format in query parameters should be dd/mm/yyyy
      //  - Missing fromDate and/or toDate will see us defaulting to a fromDate of 1970/01/01 and a toDate of 3000/01/01 - toDate= with no value will be treated as missing
      //  - Bad date e.g toDate=01/02s/2025 handled in HmppsPrisonSubjectAccessRequestService and returns a 400 response - Validation failure: Parameter toDate must be of type java.time.LocalDate
      //  - Dates in other formats e.g. 1980/01/01 will result in a 400 response

      val queryFromDate: ZonedDateTime = when (fromDate) {
        null -> ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
        else -> fromDate!!.atStartOfDay(ZoneId.systemDefault())
      }

      val queryToDate: ZonedDateTime = when (toDate) {
        null -> ZonedDateTime.of(3000, 1, 1, 23, 59, 59, 999999999, ZoneId.systemDefault())
        else -> toDate!!.atStartOfDay(ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59).withNano(999999999)
      }

      val prisonerHealthHistoryWithinTimeframe: SortedSet<FieldHistory>? =
        fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(
          prn,
          queryFromDate,
          queryToDate,
        )

      val fieldsAlreadyFound = prisonerHealthHistoryWithinTimeframe?.map { it.field } ?: emptyList()

      val latestPrisonerHistoryBeforeFromDate: List<FieldHistory> = HealthAndMedicationField.entries
        .filterNot { it in fieldsAlreadyFound }
        .mapNotNull { missingField ->
          fieldHistoryRepository.findFirstByPrisonerNumberAndFieldAndCreatedAtBeforeOrderByCreatedAtDesc(
            prn,
            missingField,
            queryFromDate,
          )
        }

      val combinedPrisonerHistory: SortedSet<FieldHistory> = sortedSetOf(compareByDescending<FieldHistory> { it.fieldHistoryId }).apply {
        prisonerHealthHistoryWithinTimeframe?.let { addAll(it) }
        latestPrisonerHistoryBeforeFromDate?.let { addAll(it) }
      }

      // Must return 204 if there is no data
      if (combinedPrisonerHistory == null || combinedPrisonerHistory!!.isEmpty()) {
        return null // Returns 204 and empty response body
      }

      // Note that Smoker information is stored in NOMIS as the Health and Medication API uses the Prison API to view and
      // edit smoker information.  The existing SAR reporting process will therefore be used to report Smoker information
      // relating to a prisoner.

      // Transform data for SAR report
      //  - Replace fieldHistoryType and reference data IDs across 4 categories i.e. FOOD_ALLERGY,
      //    MEDICAL_DIET, PERSONALISED_DIET, CATERING_INSTRUCTIONS with human-readable description.
      //  - Remove unnecessary fields
      //  - Translate internal identifiers such as reference data identifiers to the human-readable
      //    description or 'UNKNOWN_REFERENCE_DATA_DESCRIPTION if the code cannot be translated.
      //  - Flatten structure by brining important field data to the top e.g. fieldHistoryValue is set
      //    to the value.valueJson.value.allergies array rather than retaining the nested structure.

      var transformedSubjectAccessRequestData: List<SubjectAccessRequestResponseDto> =
        combinedPrisonerHistory.mapIndexed { index: Int, value: FieldHistory ->
          SubjectAccessRequestResponseDto(
            fieldHistoryId = value.fieldHistoryId,
            prisonerNumber = value.prisonerNumber,
            createdBy = value.createdBy,
            createdAt = value.createdAt,
            fieldHistoryType = when (value.field.toString()) { // // HealthAndMedicationField
              HealthAndMedicationField.FOOD_ALLERGY.toString() -> SubjectAccessRequestFieldHistoryType.FOOD_ALLERGY.description
              HealthAndMedicationField.MEDICAL_DIET.toString() -> SubjectAccessRequestFieldHistoryType.MEDICAL_DIET.description
              HealthAndMedicationField.PERSONALISED_DIET.toString() -> SubjectAccessRequestFieldHistoryType.PERSONALISED_DIET.description
              HealthAndMedicationField.CATERING_INSTRUCTIONS.toString() -> SubjectAccessRequestFieldHistoryType.CATERING_INSTRUCTIONS.description
              else -> "Unknown"
            },
            fieldHistoryValue = when (value.field.toString()) {
              "FOOD_ALLERGY" -> {
                val fah: FoodAllergyHistory? = value?.valueJson?.value as FoodAllergyHistory?
                fah?.allergies?.mapIndexed { i: Int, allergy: FoodAllergyHistoryItem ->
                  val rd: ReferenceDataCode? = toReferenceDataCodeWrapped(allergy?.value)
                  allergy?.copy(
                    value = if (rd != null) rd.description else UNKNOWN_REFERENCE_DATA_DESCRIPTION,
                    comment = allergy?.comment,
                  )
                }
              }
              "MEDICAL_DIET" -> {
                val mdr: MedicalDietaryRequirementHistory? = value?.valueJson?.value as MedicalDietaryRequirementHistory?
                mdr?.medicalDietaryRequirements?.mapIndexed { i: Int, medicalDietaryRequirements: MedicalDietaryRequirementItem ->
                  val rd: ReferenceDataCode? = toReferenceDataCodeWrapped(medicalDietaryRequirements?.value)
                  medicalDietaryRequirements?.copy(
                    value = if (rd != null) rd.description else UNKNOWN_REFERENCE_DATA_DESCRIPTION,
                    comment = medicalDietaryRequirements?.comment,
                  )
                }
              }
              "PERSONALISED_DIET" -> {
                var pdr: PersonalisedDietaryRequirementHistory? = value?.valueJson?.value as PersonalisedDietaryRequirementHistory?
                pdr?.personalisedDietaryRequirements?.mapIndexed { i: Int, personalisedDietaryRequirements: PersonalisedDietaryRequirementItem ->
                  val rd: ReferenceDataCode? = toReferenceDataCodeWrapped(personalisedDietaryRequirements?.value)
                  personalisedDietaryRequirements?.copy(
                    value = if (rd != null) rd.description else UNKNOWN_REFERENCE_DATA_DESCRIPTION,
                    comment = personalisedDietaryRequirements?.comment,
                  )
                }
              }
              "CATERING_INSTRUCTIONS" -> value?.valueString
              else -> ""
            },
            mergedAt = value.mergedAt,
            mergedFrom = value.mergedFrom,
            prisonId = value.prisonId,
          )
        }.sortedByDescending { it.fieldHistoryId } // Most recent first

      return HmppsSubjectAccessRequestContent(
        content = transformedSubjectAccessRequestData as List<SubjectAccessRequestResponseDto>,
      )
    } catch (e: Exception) {
      throw e // Return 500
    }
  }

  private fun toReferenceDataCodeWrapped(id: String?): ReferenceDataCode? {
    try {
      return toReferenceDataCode(referenceDataCodeRepository, id)
    } catch (e: IllegalArgumentException) {
    }
    return null
  }
}
