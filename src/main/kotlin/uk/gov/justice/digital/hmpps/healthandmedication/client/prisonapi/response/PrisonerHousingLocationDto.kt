package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response

data class PrisonerHousingLocationDto(
  val levels: List<HousingLevelDto>?,
)

data class HousingLevelDto(
  val level: Int,
  val code: String,
  val description: String,
)
