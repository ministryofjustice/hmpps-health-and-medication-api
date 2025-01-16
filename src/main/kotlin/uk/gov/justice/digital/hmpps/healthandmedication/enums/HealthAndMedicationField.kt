package uk.gov.justice.digital.hmpps.healthandmedication.enums

import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergies
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.JsonObject
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirements
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode

private val getInt: (FieldValues) -> Int? = { it.valueInt }
private val getString: (FieldValues) -> String? = { it.valueString }
private val getRef: (FieldValues) -> ReferenceDataCode? = { it.valueRef }
private val getJson: (FieldValues) -> JsonObject? = { it.valueJson }

private val setInt: (FieldValues, Any?) -> Unit = { values, value -> values.valueInt = value as Int? }
private val setString: (FieldValues, Any?) -> Unit = { values, value -> values.valueString = value as String? }
private val setRef: (FieldValues, Any?) -> Unit = { values, value -> values.valueRef = value as ReferenceDataCode? }

private val hasChangedInt: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueInt }
private val hasChangedString: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueString }
private val hasChangedRef: (old: FieldValues, new: Any?) -> Boolean = { old, new -> new != old.valueRef }

enum class HealthAndMedicationField(
  val get: (FieldValues) -> Any?,
  val set: (FieldValues, Any?) -> Unit,
  val hasChangedFrom: (FieldValues, Any?) -> Boolean,
  val domain: String?,
) {
  FOOD_ALLERGY(
    getJson,
    { values, value ->
      run {
        value as MutableSet<FoodAllergy>
        values.valueJson = JsonObject(FOOD_ALLERGY, FoodAllergies(value))
      }
    },
    { old, new -> old.valueJson?.value != FoodAllergies(new as MutableSet<FoodAllergy>) },
    "FOOD_ALLERGY",
  ),

  MEDICAL_DIET(
    getJson,
    { values, value ->
      run {
        value as MutableSet<MedicalDietaryRequirement>
        values.valueJson = JsonObject(MEDICAL_DIET, MedicalDietaryRequirements(value))
      }
    },
    { old, new -> old.valueJson?.value != MedicalDietaryRequirements(new as MutableSet<MedicalDietaryRequirement>) },
    "MEDICAL_DIET",
  ),
}

interface FieldValues {
  var valueInt: Int?
  var valueString: String?
  var valueRef: ReferenceDataCode?
  var valueJson: JsonObject?
}
