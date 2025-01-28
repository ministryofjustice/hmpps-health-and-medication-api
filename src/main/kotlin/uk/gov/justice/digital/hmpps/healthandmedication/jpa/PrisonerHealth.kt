package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Id
import jakarta.persistence.MapKey
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.SortNatural
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.DietAndAllergyDto
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.ReferenceDataSelection
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.MEDICAL_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.PERSONALISED_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.mapper.toSimpleDto
import java.time.ZonedDateTime
import java.util.SortedSet
import kotlin.reflect.KMutableProperty0

@Entity
@Table(name = "health")
class PrisonerHealth(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  override val prisonerNumber: String,

  @OneToMany(mappedBy = "prisonerNumber", cascade = [ALL], orphanRemoval = true)
  var foodAllergies: MutableSet<FoodAllergy> = mutableSetOf(),

  @OneToMany(mappedBy = "prisonerNumber", cascade = [ALL], orphanRemoval = true)
  var medicalDietaryRequirements: MutableSet<MedicalDietaryRequirement> = mutableSetOf(),

  @OneToMany(mappedBy = "prisonerNumber", cascade = [ALL], orphanRemoval = true)
  var personalisedDietaryRequirements: MutableSet<PersonalisedDietaryRequirement> = mutableSetOf(),

  // Stores snapshots of each update to a prisoner's health information
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @SortNatural
  override val fieldHistory: SortedSet<FieldHistory> = sortedSetOf(),

  // Stores timestamps of when each individual field was changed
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @MapKey(name = "field")
  override val fieldMetadata: MutableMap<HealthAndMedicationField, FieldMetadata> = mutableMapOf(),
) : WithFieldHistory<PrisonerHealth>() {

  override fun fieldAccessors(): Map<HealthAndMedicationField, KMutableProperty0<*>> = mapOf(
    FOOD_ALLERGY to ::foodAllergies,
    MEDICAL_DIET to ::medicalDietaryRequirements,
    PERSONALISED_DIET to ::personalisedDietaryRequirements,
  )

  fun toHealthDto(): HealthDto = HealthDto(toDietAndAllergyDto())

  fun toDietAndAllergyDto(): DietAndAllergyDto = DietAndAllergyDto(
    foodAllergies = getReferenceDataListValueWithMetadata(
      foodAllergies,
      { allergies ->
        allergies.map {
          ReferenceDataSelection(it.allergy.toSimpleDto(), it.commentText)
        }
      },
      FOOD_ALLERGY,
    ),
    medicalDietaryRequirements = getReferenceDataListValueWithMetadata(
      medicalDietaryRequirements,
      { dietaryRequirements ->
        dietaryRequirements.map {
          ReferenceDataSelection(it.dietaryRequirement.toSimpleDto(), it.commentText)
        }
      },
      MEDICAL_DIET,
    ),
    personalisedDietaryRequirements = getReferenceDataListValueWithMetadata(
      personalisedDietaryRequirements,
      { dietaryRequirements ->
        dietaryRequirements.map {
          ReferenceDataSelection(it.dietaryRequirement.toSimpleDto(), it.commentText)
        }
      },
      PERSONALISED_DIET,
    ),
  )

  override fun updateFieldHistory(lastModifiedAt: ZonedDateTime, lastModifiedBy: String) = updateFieldHistory(lastModifiedAt, lastModifiedBy, allFields)

  private fun <T> getReferenceDataListValueWithMetadata(
    value: T,
    mapper: (T) -> List<ReferenceDataSelection>,
    field: HealthAndMedicationField,
  ): ValueWithMetadata<List<ReferenceDataSelection>>? = fieldMetadata[field]?.let {
    ValueWithMetadata(
      mapper(value),
      it.lastModifiedAt,
      it.lastModifiedBy,
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as PrisonerHealth

    if (prisonerNumber != other.prisonerNumber) return false
    if (foodAllergies != other.foodAllergies) return false
    if (medicalDietaryRequirements != other.medicalDietaryRequirements) return false
    if (personalisedDietaryRequirements != other.personalisedDietaryRequirements) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + foodAllergies.hashCode()
    result = 31 * result + medicalDietaryRequirements.hashCode()
    result = 31 * result + personalisedDietaryRequirements.hashCode()
    return result
  }

  companion object {
    val allFields = listOf(
      FOOD_ALLERGY,
      MEDICAL_DIET,
      PERSONALISED_DIET,
    )
  }
}
