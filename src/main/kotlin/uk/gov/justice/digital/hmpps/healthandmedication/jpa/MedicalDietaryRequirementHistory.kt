package uk.gov.justice.digital.hmpps.healthandmedication.jpa

data class MedicalDietaryRequirementItem(
  var value: String,
  var comment: String? = null,
)

data class MedicalDietaryRequirementHistory(val medicalDietaryRequirements: List<MedicalDietaryRequirementItem>) {
  constructor(medicalDietaryRequirements: Collection<MedicalDietaryRequirement>) :
    this(
      medicalDietaryRequirements
        .map { it.toHistoryObject() }
        .sortedBy { it.value },
    )

  constructor(vararg medicalDietaryRequirements: String) : this(
    medicalDietaryRequirements.asList().map { MedicalDietaryRequirementItem(it) }
      .sortedBy { it.value },
  )
}
