package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.Session
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.CateringInstructions
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerLocation
import uk.gov.justice.digital.hmpps.healthandmedication.utils.toReferenceDataCode

class PrisonerHealthRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: PrisonerHealthRepository

  @Autowired
  lateinit var entityManager: EntityManager

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
  fun `can get health info for specified prisoners`() {
    val secondPrisonerNumber = "A1234AA"
    val eggAllergy = generateAllergy("FOOD_ALLERGY_EGG")
    val milkAllergy = generateAllergy("FOOD_ALLERGY_MILK")
    val nutrientDeficiency = generateMedicalDietaryRequirement("MEDICAL_DIET_NUTRIENT_DEFICIENCY")

    val firstPrisonerHealth = PrisonerHealth(
      prisonerNumber = PRISONER_NUMBER,
      foodAllergies = mutableSetOf(eggAllergy, milkAllergy),
      medicalDietaryRequirements = mutableSetOf(nutrientDeficiency),
    )
    val secondPrisonerHealth = PrisonerHealth(
      prisonerNumber = secondPrisonerNumber,
      foodAllergies = mutableSetOf(
        generateAllergy("FOOD_ALLERGY_MILK", secondPrisonerNumber),
        generateAllergy("FOOD_ALLERGY_TREE_NUTS", secondPrisonerNumber),
      ),
      personalisedDietaryRequirements = mutableSetOf(
        generatePersonalisedDietaryRequirement("PERSONALISED_DIET_VEGAN", secondPrisonerNumber),
      ),
    )

    save(firstPrisonerHealth)
    save(secondPrisonerHealth)

    val response = repository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))
    assertThat(response.size).isEqualTo(1)
    assertThat(response[0]).isEqualTo(
      PrisonerHealth(
        prisonerNumber = PRISONER_NUMBER,
        foodAllergies = mutableSetOf(eggAllergy, milkAllergy),
        medicalDietaryRequirements = mutableSetOf(nutrientDeficiency),
      ),
    )
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

  @Test
  fun `Correctly identifies records requiring migration`() {
    listOf(
      PrisonerHealth(
        prisonerNumber = "M1111AA",
        foodAllergies = mutableSetOf(generateAllergy("FOOD_ALLERGY_EGG", prisonerNumber = "M1111AA")),
      ),
      PrisonerHealth(
        prisonerNumber = "M1111BB",
        foodAllergies = mutableSetOf(generateAllergy("FOOD_ALLERGY_EGG", prisonerNumber = "M1111AA")),
        location = PrisonerLocation("M1111BB", "MDI", "E", "E-1-009"),
      ),
      PrisonerHealth(
        prisonerNumber = "M2222AA",
        medicalDietaryRequirements = mutableSetOf(
          generateMedicalDietaryRequirement("MEDICAL_DIET_LACTOSE_INTOLERANT", prisonerNumber = "M2222AA"),
        ),
      ),
      PrisonerHealth(
        prisonerNumber = "M2222BB",
        medicalDietaryRequirements = mutableSetOf(
          generateMedicalDietaryRequirement("MEDICAL_DIET_LACTOSE_INTOLERANT", prisonerNumber = "M2222AA"),
        ),
        location = PrisonerLocation("M1111BB", "MDI", "E", "E-1-009"),
      ),
      PrisonerHealth(
        prisonerNumber = "M3333AA",
        personalisedDietaryRequirements = mutableSetOf(
          generatePersonalisedDietaryRequirement("PERSONALISED_DIET_VEGAN", prisonerNumber = "M3333AA"),
        ),
      ),
      PrisonerHealth(
        prisonerNumber = "M3333BB",
        personalisedDietaryRequirements = mutableSetOf(
          generatePersonalisedDietaryRequirement("PERSONALISED_DIET_VEGAN", prisonerNumber = "M3333AA"),
        ),
        location = PrisonerLocation("M1111BB", "MDI", "E", "E-1-009"),
      ),
      PrisonerHealth(
        prisonerNumber = "M4444AA",
        cateringInstructions = generateCateringInstructions(prisonerNumber = "M4444AA"),
      ),
      PrisonerHealth(
        prisonerNumber = "M4444BB",
        cateringInstructions = generateCateringInstructions(prisonerNumber = "M4444BB"),
        location = PrisonerLocation("M4444BB", "MDI", "E", "E-1-009"),
      ),
    ).forEach { save(it) }

    val response = repository.findAllPrisonersWithoutLocationData()

    assertThat(response.map { it.prisonerNumber }).containsExactlyInAnyOrder("M1111AA", "M2222AA", "M3333AA", "M4444AA")
  }

  @Test
  fun `soft deleted records are ignored by findAllPrisonersWithDietaryNeeds`() {
    val health = PrisonerHealth(
      prisonerNumber = PRISONER_NUMBER,
      foodAllergies = mutableSetOf(generateAllergy("FOOD_ALLERGY_EGG")),
    )
    save(health)

    assertThat(repository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).hasSize(1)

    health.deletedAt = java.time.ZonedDateTime.now()
    save(health)

    assertThat(repository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).isEmpty()
  }

  @Test
  fun `soft deleted records are ignored by findAllPrisonersWithoutLocationData`() {
    val health = PrisonerHealth(
      prisonerNumber = PRISONER_NUMBER,
      foodAllergies = mutableSetOf(generateAllergy("FOOD_ALLERGY_EGG")),
    )
    save(health)

    assertThat(repository.findAllPrisonersWithoutLocationData()).extracting("prisonerNumber").contains(PRISONER_NUMBER)

    health.deletedAt = java.time.ZonedDateTime.now()
    save(health)

    assertThat(repository.findAllPrisonersWithoutLocationData()).extracting("prisonerNumber").doesNotContain(PRISONER_NUMBER)
  }

  @Test
  fun `findByPrisonerNumberAndDeletedAtIsNull returns empty for soft deleted record`() {
    val health = PrisonerHealth(PRISONER_NUMBER)
    save(health)

    assertThat(repository.findByPrisonerNumberAndDeletedAtIsNull(PRISONER_NUMBER)).isNotNull

    health.deletedAt = java.time.ZonedDateTime.now()
    save(health)

    assertThat(repository.findByPrisonerNumberAndDeletedAtIsNull(PRISONER_NUMBER)).isNull()
  }

  @Test
  fun `findAllPrisonersWithDietaryNeeds includes records with pending merges and filters top level only`() {
    val pendingPrisonerNumber = "B1234PN"
    val pendingRecord = createHealthWithAllergy(pendingPrisonerNumber, "FOOD_ALLERGY_PEANUTS")
    pendingRecord.pendingMergeToPrisonerNumber = PRISONER_NUMBER

    val mainRecord = PrisonerHealth(PRISONER_NUMBER)
    save(mainRecord)
    save(pendingRecord)

    val result = repository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER, pendingPrisonerNumber))
    // Should return the main record because it has a pending merge with data
    assertThat(result).hasSize(1)
    assertThat(result[0].prisonerNumber).isEqualTo(PRISONER_NUMBER)
    assertThat(result[0].pendingMerges).hasSize(1)
  }

  @Test
  fun `findAllPrisonersWithDietaryNeeds excludes blank records with only blank pending merges`() {
    val pendingPrisonerNumber = "B1234PN"
    val pendingRecord = PrisonerHealth(pendingPrisonerNumber)
    pendingRecord.pendingMergeToPrisonerNumber = PRISONER_NUMBER

    val mainRecord = PrisonerHealth(PRISONER_NUMBER)
    save(mainRecord)
    save(pendingRecord)

    val result = repository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER, pendingPrisonerNumber))
    // Both are blank, so main record should not be returned
    assertThat(result).isEmpty()
  }

  @Test
  fun `findAllPrisonersWithDietaryNeeds does not return soft deleted main record even with active pending merges`() {
    val pendingPrisonerNumber = "B1234PN"
    val pendingRecord = createHealthWithAllergy(pendingPrisonerNumber, "FOOD_ALLERGY_PEANUTS")
    pendingRecord.pendingMergeToPrisonerNumber = PRISONER_NUMBER

    val mainRecord = createHealthWithAllergy(PRISONER_NUMBER, "FOOD_ALLERGY_EGG")
    mainRecord.deletedAt = java.time.ZonedDateTime.now()

    save(mainRecord)
    save(pendingRecord)

    val result = repository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER, pendingPrisonerNumber))
    // Main record is soft-deleted, so it should not be returned at all
    assertThat(result).isEmpty()
  }

  @Test
  fun `findAllPrisonersWithDietaryNeeds ignores soft deleted pending merges`() {
    val pendingPrisonerNumber = "B1234PN"
    val pendingRecord = createHealthWithAllergy(pendingPrisonerNumber, "FOOD_ALLERGY_PEANUTS")
    pendingRecord.pendingMergeToPrisonerNumber = PRISONER_NUMBER
    pendingRecord.deletedAt = java.time.ZonedDateTime.now()

    val mainRecord = PrisonerHealth(PRISONER_NUMBER)
    save(mainRecord)
    save(pendingRecord)

    val result = repository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER, pendingPrisonerNumber))
    // Main record is blank and its only pending merge is soft-deleted
    assertThat(result).isEmpty()
  }

  @Test
  fun `pendingMerges collection filters out soft deleted records`() {
    val pendingPrisonerNumber = "B1234PN"
    val pendingRecord = PrisonerHealth(
      prisonerNumber = pendingPrisonerNumber,
      foodAllergies = mutableSetOf(generateAllergy("FOOD_ALLERGY_PEANUTS", pendingPrisonerNumber)),
      pendingMergeToPrisonerNumber = PRISONER_NUMBER,
      deletedAt = java.time.ZonedDateTime.now(),
    )
    val mainRecord = PrisonerHealth(
      prisonerNumber = PRISONER_NUMBER,
      foodAllergies = mutableSetOf(generateAllergy("FOOD_ALLERGY_EGG", PRISONER_NUMBER)),
    )
    save(mainRecord)
    save(pendingRecord)

    val result = repository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER, pendingPrisonerNumber))
    assertThat(result).hasSize(1)
    assertThat(result[0].prisonerNumber).isEqualTo(PRISONER_NUMBER)
    assertThat(result[0].pendingMerges).isEmpty()
  }

  @Test
  fun `pendingMerges are loaded in batches`() {
    // Create 30 prisoners. None of them have pending merges.
    // 30 is > 25 (default batch size).
    val mainPrisoners = (1..30).map { i ->
      val prisonerNumber = "M%06d".format(i)
      PrisonerHealth(prisonerNumber)
    }
    mainPrisoners.forEach { repository.save(it) }

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    entityManager.clear()

    val session = entityManager.unwrap(Session::class.java)
    val statistics = session.sessionFactory.statistics
    statistics.isStatisticsEnabled = true
    statistics.clear()

    // Load all main prisoners
    val ids = (1..30).map { i -> "M%06d".format(i) }
    val loadedMain = repository.findAllById(ids)
    assertThat(loadedMain).hasSize(30)
    val afterLoadCount = statistics.prepareStatementCount

    // Access pendingMerges for each.
    // Since there are no pending merges, this will only trigger the collection loads.
    // With batching, this should be 2 queries (one for 25, one for 5).
    loadedMain.forEach { it.pendingMerges.size }
    val afterAccessCount = statistics.prepareStatementCount

    val diff = afterAccessCount - afterLoadCount
    // We expect 2 queries for pendingMerges if batching works.
    // If not, we expect 30.
    assertThat(diff).isEqualTo(2)
  }

  private fun createHealthWithAllergy(prisonerNumber: String, allergyId: String) = PrisonerHealth(
    prisonerNumber = prisonerNumber,
    foodAllergies = mutableSetOf(generateAllergy(allergyId, prisonerNumber)),
  )

  private fun generateAllergy(id: String, prisonerNumber: String = PRISONER_NUMBER) = FoodAllergy(
    prisonerNumber = prisonerNumber,
    allergy = toReferenceDataCode(referenceDataCodeRepository, id)!!,
  )

  private fun generateMedicalDietaryRequirement(id: String, prisonerNumber: String = PRISONER_NUMBER) = MedicalDietaryRequirement(
    prisonerNumber = prisonerNumber,
    dietaryRequirement = toReferenceDataCode(referenceDataCodeRepository, id)!!,
  )

  private fun generatePersonalisedDietaryRequirement(id: String, prisonerNumber: String = PRISONER_NUMBER) = PersonalisedDietaryRequirement(
    prisonerNumber = prisonerNumber,
    dietaryRequirement = toReferenceDataCode(referenceDataCodeRepository, id)!!,
  )

  private fun generateCateringInstructions(
    instructions: String = "Some instructions",
    prisonerNumber: String = PRISONER_NUMBER,
  ) = CateringInstructions(
    prisonerNumber = prisonerNumber,
    instructions = instructions,
  )

  private companion object {
    const val PRISONER_NUMBER = "Z1234ZZ"
  }
}
