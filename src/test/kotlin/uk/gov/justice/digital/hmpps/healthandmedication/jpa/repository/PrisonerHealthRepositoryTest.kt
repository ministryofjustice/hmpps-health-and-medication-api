package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class PrisonerHealthRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: PrisonerHealthRepository

  fun save(prisonerHealth: PrisonerHealth) {
    repository.save(prisonerHealth)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @Test
  fun `can persist health`() {
    val prisonerHealth = PrisonerHealth(
      PRISONER_NUMBER,
      mutableSetOf(EGG_ALLERGY, MILK_ALLERGY),
      mutableSetOf(MEDICAL_DIET_LACTOSE_INTOLERANT, MEDICAL_DIET_NUTRIENT_DEFICIENCY),
    )
    save(prisonerHealth)

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(foodAllergies).hasSize(2)
      assertThat(foodAllergies).contains(EGG_ALLERGY)
      assertThat(foodAllergies).contains(MILK_ALLERGY)
      assertThat(medicalDietaryRequirements).hasSize(2)
      assertThat(medicalDietaryRequirements).contains(MEDICAL_DIET_LACTOSE_INTOLERANT)
      assertThat(medicalDietaryRequirements).contains(MEDICAL_DIET_NUTRIENT_DEFICIENCY)
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
    repository.save(PrisonerHealth(PRISONER_NUMBER))
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val health = repository.getReferenceById(PRISONER_NUMBER)
    health.foodAllergies.add(EGG_ALLERGY)
    health.medicalDietaryRequirements.add(MEDICAL_DIET_LACTOSE_INTOLERANT)

    repository.save(health)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(repository.getReferenceById(PRISONER_NUMBER)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(foodAllergies).hasSize(1)
      assertThat(foodAllergies.first()).isEqualTo(EGG_ALLERGY)
      assertThat(medicalDietaryRequirements).hasSize(1)
      assertThat(medicalDietaryRequirements.first()).isEqualTo(MEDICAL_DIET_LACTOSE_INTOLERANT)
    }
  }

  @Test
  fun `can test for equality`() {
    assertThat(
      PrisonerHealth(
        prisonerNumber = PRISONER_NUMBER,
        foodAllergies = mutableSetOf(EGG_ALLERGY),
        medicalDietaryRequirements = mutableSetOf(MEDICAL_DIET_NUTRIENT_DEFICIENCY),
      ),
    ).isEqualTo(
      PrisonerHealth(
        prisonerNumber = PRISONER_NUMBER,
        foodAllergies = mutableSetOf(EGG_ALLERGY),
        medicalDietaryRequirements = mutableSetOf(MEDICAL_DIET_NUTRIENT_DEFICIENCY),
      ),
    )

    // Prisoner number
    assertThat(PrisonerHealth(PRISONER_NUMBER)).isNotEqualTo(PrisonerHealth("Example"))

    // Allergies
    assertThat(PrisonerHealth(PRISONER_NUMBER, mutableSetOf(EGG_ALLERGY))).isNotEqualTo(
      PrisonerHealth(PRISONER_NUMBER, mutableSetOf(MILK_ALLERGY)),
    )

    // Medical diet
    assertThat(
      PrisonerHealth(PRISONER_NUMBER, mutableSetOf(EGG_ALLERGY), mutableSetOf(MEDICAL_DIET_LACTOSE_INTOLERANT)),
    ).isNotEqualTo(
      PrisonerHealth(PRISONER_NUMBER, mutableSetOf(EGG_ALLERGY), mutableSetOf(MEDICAL_DIET_NUTRIENT_DEFICIENCY)),
    )
  }

  @Test
  fun `toString does not cause stack overflow`() {
    assertThat(
      PrisonerHealth(PRISONER_NUMBER, mutableSetOf(EGG_ALLERGY), mutableSetOf(MEDICAL_DIET_LACTOSE_INTOLERANT)).toString(),
    ).isInstanceOf(String::class.java)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"

    val FOOD_ALLERGY_DOMAIN = ReferenceDataDomain("FOOD_ALLERGY", "Food allergy", 0, ZonedDateTime.now(), "OMS_OWNER")
    val EGG_ALLERGY = FoodAllergy(
      prisonerNumber = PRISONER_NUMBER,
      allergy = ReferenceDataCode(
        id = "FOOD_ALLERGY_EGG",
        domain = FOOD_ALLERGY_DOMAIN,
        code = "EGG",
        description = "Egg",
        listSequence = 3,
        createdAt = ZonedDateTime.now(),
        createdBy = "OMS_OWNER",
      ),
    )

    val MILK_ALLERGY =
      FoodAllergy(
        prisonerNumber = PRISONER_NUMBER,
        allergy = ReferenceDataCode(
          id = "FOOD_ALLERGY_MILK",
          domain = FOOD_ALLERGY_DOMAIN,
          code = "MILK",
          description = "Milk",
          listSequence = 6,
          createdAt = ZonedDateTime.now(),
          createdBy = "OMS_OWNER",
        ),
      )

    val MEDICAL_DIET_DOMAIN = ReferenceDataDomain(
      "MEDICAL_DIET",
      "Medical diet",
      0,
      ZonedDateTime.now(),
      "CONNECT_DPS",
    )

    val MEDICAL_DIET_NUTRIENT_DEFICIENCY =
      MedicalDietaryRequirement(
        prisonerNumber = PRISONER_NUMBER,
        dietaryRequirement = ReferenceDataCode(
          id = "MEDICAL_DIET_NUTRIENT_DEFICIENCY",
          domain = MEDICAL_DIET_DOMAIN,
          code = "NUTRIENT_DEFICIENCY",
          description = "Nutrient deficiency",
          listSequence = 7,
          createdAt = ZonedDateTime.now(),
          createdBy = "CONNECT_DPS",
        ),
      )

    val MEDICAL_DIET_LACTOSE_INTOLERANT =
      MedicalDietaryRequirement(
        prisonerNumber = PRISONER_NUMBER,
        dietaryRequirement = ReferenceDataCode(
          id = "MEDICAL_DIET_LACTOSE_INTOLERANT",
          domain = MEDICAL_DIET_DOMAIN,
          code = "LACTOSE_INTOLERANT",
          description = "Lactose intolerant",
          listSequence = 5,
          createdAt = ZonedDateTime.now(),
          createdBy = "CONNECT_DPS",
        ),
      )
  }
}
