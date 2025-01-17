package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.dto

data class PrisonerDto(
  val prisonerNumber: String,
  val prisonId: String? = null,
)
