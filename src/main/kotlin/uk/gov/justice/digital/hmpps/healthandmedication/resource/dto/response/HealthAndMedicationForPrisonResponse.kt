package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response

import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.PageMeta

data class HealthAndMedicationForPrisonDto(
  val prisonerNumber: String,
  val firstName: String?,
  val lastName: String?,
  val location: String?,
  val health: HealthAndMedicationResponse,
)

data class HealthAndMedicationForPrisonResponse(
  val content: List<HealthAndMedicationForPrisonDto>,
  val metadata: PageMeta,
)
