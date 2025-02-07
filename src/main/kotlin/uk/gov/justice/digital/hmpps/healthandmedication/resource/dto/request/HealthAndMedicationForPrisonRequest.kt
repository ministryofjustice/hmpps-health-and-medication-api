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
) : PagedRequest
