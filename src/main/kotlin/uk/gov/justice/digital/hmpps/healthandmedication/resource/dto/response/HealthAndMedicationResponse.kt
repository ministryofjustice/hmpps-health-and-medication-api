package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Prisoner health and medication data")
data class HealthAndMedicationResponse(
  @Schema(description = "Diet and allergy")
  val dietAndAllergy: DietAndAllergyResponse? = null,
)
