package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.response

data class PrisonerDto(
  val prisonerNumber: String,
  val prisonId: String? = null,
  val firstName: String? = null,
  val lastName: String? = null,
  val cellLocation: String? = null,
)
