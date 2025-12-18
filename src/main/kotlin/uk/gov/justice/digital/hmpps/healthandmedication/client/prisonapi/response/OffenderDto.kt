package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response

data class OffenderDto(
  val offenderNo: String,
  val assignedLivingUnit: AssignedLivingUnitDto? = null,
)

data class AssignedLivingUnitDto(
  val agencyId: String?,
  val agencyName: String?,
  val description: String?,
  val locationId: Long?,
)
