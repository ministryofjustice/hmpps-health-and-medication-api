package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.Min

interface PagedRequest {
  @get:Parameter(description = "The page to request, starting at 1", example = "1")
  @get:Min(value = 1, message = "Page number must be at least 1")
  val page: Int

  @get:Parameter(description = "The page size to request", example = "10")
  @get:Min(value = 1, message = "Page size must be at least 1")
  val size: Int
  val sort: String?

  fun validSortFields(): Set<String> = setOf("prisonerName", "location")
}

data class PageMeta(
  val first: Boolean,
  val last: Boolean,
  val numberOfElements: Int,
  val offset: Int,
  val pageNumber: Int,
  val size: Int,
  val totalElements: Int,
  val totalPages: Int,
)

data class HealthAndMedicationForPrisonRequest(
  override val page: Int,
  override val size: Int,

  @Parameter(description = "The sort to apply to the results", example = "prisonerNumber,desc")
  override val sort: String = "",

  @Parameter(
    description = "Optional filters to apply to the results. Returned results will match at least one " +
      "filter, but do not need to match all filters (i.e. filters are applied using OR logic)",
  )
  val filters: HealthAndMedicationRequestFilters? = null,
) : PagedRequest

data class HealthAndMedicationRequestFilters(
  @Parameter(description = "Food allergy filters. Should match FOOD_ALLERGY reference data codes")
  val foodAllergies: Set<String> = emptySet(),
  @Parameter(description = "Medical diet filters. Should match MEDICAL_DIET reference data codes")
  val medicalDietaryRequirements: Set<String> = emptySet(),
  @Parameter(description = "Personalised diet filters. Should match PERSONALISED_DIET reference data codes")
  val personalisedDietaryRequirements: Set<String> = emptySet(),
)
