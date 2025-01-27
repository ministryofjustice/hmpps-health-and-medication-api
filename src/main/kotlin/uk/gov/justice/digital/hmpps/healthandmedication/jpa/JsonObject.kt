package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField

data class JsonObject(
  val field: HealthAndMedicationField,

  @JsonTypeInfo(use = NAME, property = "field", include = EXTERNAL_PROPERTY)
  @JsonSubTypes(
    JsonSubTypes.Type(value = FoodAllergyHistory::class, name = "FOOD_ALLERGY"),
    JsonSubTypes.Type(value = MedicalDietaryRequirementHistory::class, name = "MEDICAL_DIET"),
    JsonSubTypes.Type(value = PersonalisedDietaryRequirementHistory::class, name = "PERSONALISED_DIET"),
  )
  val value: Any?,
)
