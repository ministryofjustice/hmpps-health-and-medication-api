package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Diet and allergy data")
data class DietAndAllergyResponse(
  @Schema(description = "Food allergies")
  val foodAllergies: ValueWithMetadata<List<ReferenceDataSelection>>? = null,

  @Schema(description = "Medical dietary requirements")
  val medicalDietaryRequirements: ValueWithMetadata<List<ReferenceDataSelection>>? = null,

  @Schema(description = "Personalised dietary requirements")
  val personalisedDietaryRequirements: ValueWithMetadata<List<ReferenceDataSelection>>? = null,

  @Schema(description = "Catering instructions")
  val cateringInstructions: ValueWithMetadata<String?>? = null,

  @Schema(description = "The top level of the prisoner's location hierarchy e.g. Wing")
  val topLevelLocation: String? = null,

  @Schema(description = "The latest arrival date of the prisoner into prison")
  val lastAdmissionDate: LocalDate? = null,
)
