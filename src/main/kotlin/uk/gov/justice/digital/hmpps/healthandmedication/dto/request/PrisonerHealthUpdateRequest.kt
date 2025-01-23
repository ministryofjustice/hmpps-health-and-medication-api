package uk.gov.justice.digital.hmpps.healthandmedication.dto.request

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.StringToClassMapItem
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.NullishReferenceDataCodeList
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish
import uk.gov.justice.digital.hmpps.healthandmedication.utils.getAttributeAsNullish

private abstract class StringList : List<String>

@Schema(
  description = "Request object for updating a prisoner's health information. Can include one or multiple fields. " +
    "If an attribute is not provided it is not updated. Valid reference codes for `foodAllergies` can be retrieved by " +
    "querying `GET /reference-data/domains/FOOD_ALLERGY` and `GET /reference-data/domains/MEDICAL_DIET` for " +
    "`medicalDietaryRequirements`.",
  type = "object",
  properties = [
    StringToClassMapItem(key = "foodAllergies", value = StringList::class),
    StringToClassMapItem(key = "medicalDietaryRequirements", value = StringList::class),
  ],
  example = """
    {
      "foodAllergies": ["FOOD_ALLERGY_EGG", "FOOD_ALLERGY_MILK"],
      "medicalDietaryRequirements": ["MEDICAL_DIET_LOW_CHOLESTEROL"]
    }
    """,
)
@JsonInclude(NON_NULL)
data class PrisonerHealthUpdateRequest(
  @Schema(hidden = true)
  @JsonAnySetter
  private val attributes: MutableMap<String, Any?> = mutableMapOf(),
) {
  @field:NullishReferenceDataCodeList(domains = ["FOOD_ALLERGY"])
  @JsonIgnore
  val foodAllergies: Nullish<List<String>> =
    getAttributeAsNullish<List<String>>(attributes, "foodAllergies")

  @field:NullishReferenceDataCodeList(domains = ["MEDICAL_DIET"])
  @JsonIgnore
  val medicalDietaryRequirements: Nullish<List<String>> =
    getAttributeAsNullish<List<String>>(attributes, "medicalDietaryRequirements")
}
