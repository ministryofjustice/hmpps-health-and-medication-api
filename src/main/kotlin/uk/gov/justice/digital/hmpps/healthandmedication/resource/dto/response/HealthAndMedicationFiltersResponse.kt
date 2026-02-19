package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Diet and allergy data")
data class HealthAndMedicationFiltersResponse(
  @Schema(description = "Food allergy filters")
  val foodAllergies: List<HealthAndMedicationFilter> = emptyList(),

  @Schema(description = "Medical diet filters")
  val medicalDietaryRequirements: List<HealthAndMedicationFilter> = emptyList(),

  @Schema(description = "Personalised diet filters")
  val personalisedDietaryRequirements: List<HealthAndMedicationFilter> = emptyList(),

  @Schema(description = "Location filters")
  val topLocationLevel: List<HealthAndMedicationFilter> = emptyList(),

  @Schema(description = "Recent arrival filter")
  val recentArrival: HealthAndMedicationFilter? = null,
)

@Schema(description = "Health and medication filter based on reference data")
data class HealthAndMedicationFilter(
  @Schema(description = "The name of the filter")
  val name: String,

  @Schema(description = "The filter value (reference code)")
  val value: String,

  @Schema(description = "The number of prisoners in the prison matching this filter")
  val count: Int,
)
