package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.ReferenceDataListValidation

@Schema(
  description = "Request object for creating or updating diet and allergy information for a prisoner.",
)
@JsonInclude(NON_NULL)
data class UpdateDietAndAllergyRequest(
  @Schema(
    description = "The list of food allergies the prisoner has with an optional comment text.  " +
      "Valid `ReferenceDataCode.id` options for `foodAllergies` can be retrieved by querying " +
      "`GET /reference-data/domains/FOOD_ALLERGY`.",
    example = """[{ "value": "FOOD_ALLERGY_EGG" }, { "value": "FOOD_ALLERGY_OTHER", "comment": "Kohlrabi" }]""",
    requiredMode = REQUIRED,
    nullable = false,
  )
  @field:ReferenceDataListValidation(domains = ["FOOD_ALLERGY"])
  val foodAllergies: List<ReferenceDataIdSelection>?,

  @Schema(
    description =
    "The list of medical dietary requirements the prisoner has with an optional comment text.  " +
      "Valid `ReferenceDataCode.id` options for `medicalDietaryRequirements` can be retrieved by querying " +
      "`GET /reference-data/domains/MEDICAL_DIET`.",
    example = """[{ "value": "MEDICAL_DIET_LOW_CHOLESTEROL" }, { "value": "MEDICAL_DIET_OTHER", "comment": "Some other diet" }]""",
    requiredMode = REQUIRED,
    nullable = false,
  )
  @field:ReferenceDataListValidation(domains = ["MEDICAL_DIET"])
  val medicalDietaryRequirements: List<ReferenceDataIdSelection>?,

  @Schema(
    description =
    "The list of personalised dietary requirements the prisoner has with an optional comment text.  " +
      "Valid `ReferenceDataCode.id` options for `personalisedDietaryRequirements` can be retrieved by querying " +
      "`GET /reference-data/domains/PERSONALISED_DIET`.",
    example = """[{ "value": "PERSONALISED_DIET_VEGAN" }, { "value": "PERSONALISED_DIET_OTHER", "comment": "Some other diet" }]""",
    requiredMode = REQUIRED,
    nullable = false,
  )
  @field:ReferenceDataListValidation(domains = ["PERSONALISED_DIET"])
  val personalisedDietaryRequirements: List<ReferenceDataIdSelection>?,

  @Schema(
    description = "A description of specific catering instructions required by the prisoner.",
    example = "Some specific instructions.",
    requiredMode = REQUIRED,
    nullable = true,
  )
  val cateringInstructions: String?,
)
