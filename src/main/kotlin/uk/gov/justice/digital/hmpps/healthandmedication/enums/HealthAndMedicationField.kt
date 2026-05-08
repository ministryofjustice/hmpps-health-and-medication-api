package uk.gov.justice.digital.hmpps.healthandmedication.enums

import uk.gov.justice.digital.hmpps.healthandmedication.jpa.CateringInstructions
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergyHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.JsonObject
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirementHistory
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

private inline fun <reified E> Any?.toMutableSetOf(): MutableSet<E> = (this as Collection<*>).filterIsInstanceTo(mutableSetOf())

enum class HealthAndMedicationField(
  val get: (FieldValues) -> Any?,
  val set: (FieldValues, Any?) -> Unit,
  val hasChangedFrom: (FieldValues, Any?) -> Boolean,
  val domain: String?,
) {
  FOOD_ALLERGY(
    getJson,
    { values, value ->
      values.valueJson = JsonObject(FOOD_ALLERGY, FoodAllergyHistory(value.toMutableSetOf<FoodAllergy>()))
    },
    { old, new -> old.valueJson?.value != FoodAllergyHistory(new.toMutableSetOf<FoodAllergy>()) },
    "FOOD_ALLERGY",
  ),

  MEDICAL_DIET(
    getJson,
    { values, value ->
      values.valueJson =
        JsonObject(MEDICAL_DIET, MedicalDietaryRequirementHistory(value.toMutableSetOf<MedicalDietaryRequirement>()))
    },
    { old, new -> old.valueJson?.value != MedicalDietaryRequirementHistory(new.toMutableSetOf<MedicalDietaryRequirement>()) },
    "MEDICAL_DIET",
  ),

  PERSONALISED_DIET(
    getJson,
    { values, value ->
      values.valueJson = JsonObject(
        PERSONALISED_DIET,
        PersonalisedDietaryRequirementHistory(value.toMutableSetOf<PersonalisedDietaryRequirement>()),
      )
    },
    { old, new -> old.valueJson?.value != PersonalisedDietaryRequirementHistory(new.toMutableSetOf<PersonalisedDietaryRequirement>()) },
    "PERSONALISED_DIET",
  ),

  CATERING_INSTRUCTIONS(
    getString,
    { values, value ->
      run {
        value as CateringInstructions?
        values.valueString = value?.instructions
      }
    },
    { old, new -> (new as CateringInstructions?)?.instructions != old.valueString },
    "CATERING_INSTRUCTIONS",
  ),
}

interface FieldValues {
  var valueInt: Int?
  var valueString: String?
  var valueRef: ReferenceDataCode?
  var valueJson: JsonObject?
}
