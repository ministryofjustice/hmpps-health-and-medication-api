package uk.gov.justice.digital.hmpps.healthandmedication.jpa

data class FoodAllergyHistoryItem(
  var value: String,
  var comment: String? = null,
)

data class FoodAllergyHistory(val allergies: List<FoodAllergyHistoryItem>) {
  constructor(allergies: Collection<FoodAllergy>) : this(allergies.map { it.toHistoryObject() }.sortedBy { it.value })
  constructor(vararg allergies: String) : this(
    allergies.asList().map { FoodAllergyHistoryItem(it) }
      .sortedBy { it.value },
  )
}
