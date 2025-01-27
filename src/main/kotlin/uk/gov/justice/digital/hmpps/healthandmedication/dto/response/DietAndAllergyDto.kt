package uk.gov.justice.digital.hmpps.healthandmedication.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Diet and allergy data")
data class DietAndAllergyDto(
  @Schema(description = "Food allergies")
  val foodAllergies: ValueWithMetadata<List<ReferenceDataSelection>>? = null,

  @Schema(description = "Medical dietary requirements")
  val medicalDietaryRequirements: ValueWithMetadata<List<ReferenceDataSelection>>? = null,

  @Schema(description = "Personalised dietary requirements")
  val personalisedDietaryRequirements: ValueWithMetadata<List<ReferenceDataSelection>>? = null,
)
