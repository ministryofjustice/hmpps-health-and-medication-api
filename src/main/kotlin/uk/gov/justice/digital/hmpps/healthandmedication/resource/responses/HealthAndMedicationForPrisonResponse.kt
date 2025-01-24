package uk.gov.justice.digital.hmpps.healthandmedication.resource.responses

import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.healthandmedication.resource.requests.PageMeta

data class HealthAndMedicationForPrisonDto(
  val prisonerNumber: String,
  val firstName: String?,
  val lastName: String?,
  val location: String?,
  val health: HealthDto,
)

data class HealthAndMedicationForPrisonResponse(
  val content: List<HealthAndMedicationForPrisonDto>,
  val metadata: PageMeta,
)
