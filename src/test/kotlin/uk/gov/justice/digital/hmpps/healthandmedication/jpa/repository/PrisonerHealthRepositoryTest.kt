package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.utils.toReferenceDataCode

class PrisonerHealthRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: PrisonerHealthRepository

  @Autowired
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  fun save(prisonerHealth: PrisonerHealth) {
    repository.save(prisonerHealth)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @Test
  fun `can persist health`() {
    val eggAllergy = generateAllergy("FOOD_ALLERGY_EGG")
    val milkAllergy = generateAllergy("FOOD_ALLERGY_MILK")

    val nutrientDeficiency = generateMedicalDietaryRequirement("MEDICAL_DIET_NUTRIENT_DEFICIENCY")
    val lactoseIntolerance = generateMedicalDietaryRequirement("MEDICAL_DIET_LACTOSE_INTOLERANT")

    val vegan = generatePersonalisedDietaryRequirement("PERSONALISED_DIET_VEGAN")

    val prisonerHealth = PrisonerHealth(
      prisonerNumber = PRISONER_NUMBER,
      foodAllergies = mutableSetOf(eggAllergy, milkAllergy),
      medicalDietaryRequirements = mutableSetOf(nutrientDeficiency, lactoseIntolerance),
      personalisedDietaryRequirements = mutableSetOf(vegan),
    )

    save(prisonerHealth)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(foodAllergies).hasSize(2)
      assertThat(foodAllergies).contains(eggAllergy)
      assertThat(foodAllergies).contains(milkAllergy)
      assertThat(medicalDietaryRequirements).hasSize(2)
      assertThat(medicalDietaryRequirements).contains(lactoseIntolerance)
      assertThat(medicalDietaryRequirements).contains(nutrientDeficiency)
      assertThat(personalisedDietaryRequirements).hasSize(1)
      assertThat(personalisedDietaryRequirements).contains(vegan)
    }
  }

  @Test
  fun `can persist health with null values`() {
    val prisonerHealth = PrisonerHealth(PRISONER_NUMBER)
    save(prisonerHealth)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(foodAllergies).isEmpty()
      assertThat(medicalDietaryRequirements).isEmpty()
    }
  }

  @Test
  fun `can update health`() {
    val eggAllergy = generateAllergy("FOOD_ALLERGY_EGG")
    val nutrientDeficiency = generateMedicalDietaryRequirement("MEDICAL_DIET_NUTRIENT_DEFICIENCY")
    val vegan = generatePersonalisedDietaryRequirement("PERSONALISED_DIET_VEGAN")

    repository.save(PrisonerHealth(PRISONER_NUMBER))
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val health = repository.getReferenceById(PRISONER_NUMBER)
    health.foodAllergies.add(eggAllergy)
    health.medicalDietaryRequirements.add(nutrientDeficiency)
    health.personalisedDietaryRequirements.add(vegan)

    repository.save(health)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(foodAllergies).containsExactly(eggAllergy)
      assertThat(medicalDietaryRequirements).containsExactly(nutrientDeficiency)
      assertThat(personalisedDietaryRequirements).containsExactly(vegan)
    }
  }

  @Test
  fun `can test for equality`() {
    val eggAllergy = generateAllergy("FOOD_ALLERGY_EGG")
    val milkAllergy = generateAllergy("FOOD_ALLERGY_MILK")

    val nutrientDeficiency = generateMedicalDietaryRequirement("MEDICAL_DIET_NUTRIENT_DEFICIENCY")
    val lactoseIntolerance = generateMedicalDietaryRequirement("MEDICAL_DIET_LACTOSE_INTOLERANT")

    val veganDiet = generatePersonalisedDietaryRequirement("PERSONALISED_DIET_VEGAN")
    val kosherDiet = generatePersonalisedDietaryRequirement("PERSONALISED_DIET_KOSHER")

    assertThat(
      PrisonerHealth(
        prisonerNumber = PRISONER_NUMBER,
        foodAllergies = mutableSetOf(eggAllergy),
        medicalDietaryRequirements = mutableSetOf(nutrientDeficiency),
        personalisedDietaryRequirements = mutableSetOf(veganDiet),
      ),
    ).isEqualTo(
      PrisonerHealth(
        prisonerNumber = PRISONER_NUMBER,
        foodAllergies = mutableSetOf(eggAllergy),
        medicalDietaryRequirements = mutableSetOf(nutrientDeficiency),
        personalisedDietaryRequirements = mutableSetOf(veganDiet),
      ),
    )

    // Prisoner number
    assertThat(PrisonerHealth(PRISONER_NUMBER)).isNotEqualTo(PrisonerHealth("Example"))

    // Allergies
    assertThat(PrisonerHealth(PRISONER_NUMBER, mutableSetOf(eggAllergy)))
      .isNotEqualTo(PrisonerHealth(PRISONER_NUMBER, mutableSetOf(milkAllergy)))

    // Medical diet
    assertThat(
      PrisonerHealth(prisonerNumber = PRISONER_NUMBER, medicalDietaryRequirements = mutableSetOf(nutrientDeficiency)),
    ).isNotEqualTo(
      PrisonerHealth(prisonerNumber = PRISONER_NUMBER, medicalDietaryRequirements = mutableSetOf(lactoseIntolerance)),
    )

    // Personalised diet
    assertThat(
      PrisonerHealth(prisonerNumber = PRISONER_NUMBER, personalisedDietaryRequirements = mutableSetOf(veganDiet)),
    ).isNotEqualTo(
      PrisonerHealth(prisonerNumber = PRISONER_NUMBER, personalisedDietaryRequirements = mutableSetOf(kosherDiet)),
    )
  }

  @Test
  fun `toString does not cause stack overflow`() {
    assertThat(
      PrisonerHealth(
        prisonerNumber = PRISONER_NUMBER,
        foodAllergies = mutableSetOf(generateAllergy("FOOD_ALLERGY_EGG")),
        medicalDietaryRequirements = mutableSetOf(generateMedicalDietaryRequirement("MEDICAL_DIET_LACTOSE_INTOLERANT")),
        personalisedDietaryRequirements = mutableSetOf(generatePersonalisedDietaryRequirement("PERSONALISED_DIET_VEGAN")),
      ).toString(),
    ).isInstanceOf(String::class.java)
  }

  private fun generateAllergy(id: String) = FoodAllergy(
    prisonerNumber = PRISONER_NUMBER,
    allergy = toReferenceDataCode(referenceDataCodeRepository, id)!!,
  )

  private fun generateMedicalDietaryRequirement(id: String) = MedicalDietaryRequirement(
    prisonerNumber = PRISONER_NUMBER,
    dietaryRequirement = toReferenceDataCode(referenceDataCodeRepository, id)!!,
  )

  private fun generatePersonalisedDietaryRequirement(id: String) = PersonalisedDietaryRequirement(
    prisonerNumber = PRISONER_NUMBER,
    dietaryRequirement = toReferenceDataCode(referenceDataCodeRepository, id)!!,
  )

  private companion object {
    const val PRISONER_NUMBER = "Z1234ZZ"
  }
}
