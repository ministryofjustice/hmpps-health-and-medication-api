package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response

import java.time.ZonedDateTime

// HmppsSubjectAccessRequestContent is provided by the HMPPS Kotlin Library
// See https://github.com/ministryofjustice/hmpps-kotlin-lib/blob/main/hmpps-kotlin-spring-boot-autoconfigure/src/main/kotlin/uk/gov/justice/hmpps/kotlin/sar/HmppsSubjectAccessRequestContent.kt

const val UNKNOWN_REFERENCE_DATA_DESCRIPTION = "Unknown"

enum class SubjectAccessRequestFieldHistoryType(val description: String) {
  FOOD_ALLERGY("Food Allergies"),
  MEDICAL_DIET("Medical Dietary Requirements"),
  PERSONALISED_DIET("Personalised Dietary Requirements"),
  CATERING_INSTRUCTIONS("Catering Instructions"),
}

data class SubjectAccessRequestResponseDto(
  val fieldHistoryId: Long,
  val prisonerNumber: String,
  var fieldHistoryType: String,
  var fieldHistoryValue: Any?,
  val createdAt: ZonedDateTime,
  val createdBy: String,
  val mergedAt: ZonedDateTime?,
  val mergedFrom: String?,
  val prisonId: String?,
)
