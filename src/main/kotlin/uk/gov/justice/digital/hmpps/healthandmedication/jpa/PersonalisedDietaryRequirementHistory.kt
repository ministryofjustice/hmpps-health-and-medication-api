package uk.gov.justice.digital.hmpps.healthandmedication.jpa

data class PersonalisedDietaryRequirementItem(
  var value: String,
  var comment: String? = null,
)

data class PersonalisedDietaryRequirementHistory(
  val personalisedDietaryRequirements: List<PersonalisedDietaryRequirementItem>,
) {
  constructor(personalisedDietaryRequirements: Collection<PersonalisedDietaryRequirement>) :
    this(
      personalisedDietaryRequirements
        .map { it.toHistoryObject() }
        .sortedBy { it.value },
    )

  constructor(vararg personalisedDietaryRequirements: String) : this(
    personalisedDietaryRequirements.asList().map { PersonalisedDietaryRequirementItem(it) }
      .sortedBy { it.value },
  )
}
