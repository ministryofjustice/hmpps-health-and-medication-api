package uk.gov.justice.digital.hmpps.healthandmedication.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Prisoner health data")
data class HealthDto(
  @Schema(description = "Diet and allergy")
  val dietAndAllergy: DietAndAllergyDto? = null,
)
