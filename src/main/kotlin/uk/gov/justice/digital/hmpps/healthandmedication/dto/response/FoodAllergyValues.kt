package uk.gov.justice.digital.hmpps.healthandmedication.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.healthandmedication.dto.ReferenceDataSimpleDto

@Schema(description = "Food Allergy V")
data class FoodAllergyValues(
  @Schema(description = "Food allergies")
  val foodAllergies: ValueWithMetadata<List<ReferenceDataSimpleDto>>? = null,

  @Schema(description = "Medical dietary requirements")
  val medicalDietaryRequirements: ValueWithMetadata<List<ReferenceDataSimpleDto>>? = null,
)
