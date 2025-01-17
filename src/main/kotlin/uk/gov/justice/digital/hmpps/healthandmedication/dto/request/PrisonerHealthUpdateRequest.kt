package uk.gov.justice.digital.hmpps.healthandmedication.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.NullishReferenceDataCodeList
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish
import uk.gov.justice.digital.hmpps.healthandmedication.utils.getAttributeAsNullish

@Schema(
  description = "Request object for updating a prisoner's health information. Can include one or multiple fields. " +
    "If an attribute is provided and set to 'null' it will be updated equal to 'null'. " +
    "If it is not provided it is not updated",
)
@JsonInclude(NON_NULL)
data class PrisonerHealthUpdateRequest(
  @Schema(hidden = true)
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) {
  @Schema(
    description = "The food allergies the prisoner has. A list of `ReferenceDataCode`.`id`",
    type = "string[]",
    example = "[FOOD_ALLERGY_EGG, FOOD_ALLERGY_MILK]",
    requiredMode = NOT_REQUIRED,
  )
  @field:NullishReferenceDataCodeList(
    domains = ["FOOD_ALLERGY"],
  )
  val foodAllergies: Nullish<List<String>> = getAttributeAsNullish<List<String>>(attributes, "foodAllergies")

  @Schema(
    description = "The medical dietary requirements the prisoner has. A list of `ReferenceDataCode`.`id`",
    type = "string[]",
    example = "[MEDICAL_DIET_LOW_FAT, FREE_FROM_EGG]",
    requiredMode = NOT_REQUIRED,
  )
  @field:NullishReferenceDataCodeList(
    domains = ["MEDICAL_DIET", "FREE_FROM"],
  )
  val medicalDietaryRequirements: Nullish<List<String>> =
    getAttributeAsNullish<List<String>>(attributes, "medicalDietaryRequirements")
}
