package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.CATERING_INSTRUCTIONS
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.MEDICAL_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.PERSONALISED_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.SortedSet

class FieldHistoryRepositoryTest : RepositoryTest() {

  @Test
  fun `can persist field history - valueRef`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = FOOD_ALLERGY,
      valueRef = REF_DATA_CODE,
      createdAt = NOW,
      createdBy = USER1,
      prisonId = PRISON_ID,
    )

    val id = fieldHistoryRepository.save(fieldHistory).fieldHistoryId

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    with(fieldHistoryRepository.getReferenceById(id)) {
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(field).isEqualTo(FOOD_ALLERGY)
      assertThat(valueInt).isNull()
      assertThat(valueString).isNull()
      assertThat(valueRef).isEqualTo(REF_DATA_CODE)
      assertThat(createdAt).isEqualTo(NOW)
      assertThat(createdBy).isEqualTo(USER1)
    }
  }

  @Test
  fun `fails to persist field history if multiple valueXXX properties are set`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = FOOD_ALLERGY,
      valueInt = 123,
      valueRef = REF_DATA_CODE,
      createdAt = NOW,
      createdBy = USER1,
      prisonId = PRISON_ID,
    )

    assertThrows(DataIntegrityViolationException::class.java) {
      fieldHistoryRepository.save(fieldHistory).fieldHistoryId

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()
    }
  }

  @Test
  fun `fails to persist field history if value_ref is not a valid value`() {
    val fieldHistory = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = FOOD_ALLERGY,
      valueRef = INVALID_REF_DATA_CODE,
      createdAt = NOW,
      createdBy = USER1,
      prisonId = PRISON_ID,
    )

    assertThrows(InvalidDataAccessApiUsageException::class.java) {
      fieldHistoryRepository.save(fieldHistory).fieldHistoryId

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()
    }
  }

  @Test
  fun `can check for equality`() {
    assertThat(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = FOOD_ALLERGY,
        valueRef = REF_DATA_CODE,
        createdAt = NOW,
        createdBy = USER1,
        prisonId = PRISON_ID,
      ),
    ).isEqualTo(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = FOOD_ALLERGY,
        valueRef = REF_DATA_CODE,
        createdAt = NOW,
        createdBy = USER1,
        prisonId = PRISON_ID,
      ),
    )
  }

  @Test
  fun `toString does not cause a stack overflow`() {
    assertThat(
      FieldHistory(
        prisonerNumber = PRISONER_NUMBER,
        field = FOOD_ALLERGY,
        valueRef = REF_DATA_CODE,
        createdAt = NOW,
        createdBy = USER1,
        prisonId = PRISON_ID,
      ).toString(),
    ).isInstanceOf(String::class.java)
  }

  @Test
  fun `find field history for a prisoner between two dates ordered by fieldHistoryId`() {
    val fieldHistory1 = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = FOOD_ALLERGY,
      valueRef = REF_DATA_CODE,
      createdAt = UNEXPECTED_CREATED_AT_BEFORE_FROM,
      createdBy = USER1,
      prisonId = PRISON_ID,
    )
    fieldHistoryRepository.save(fieldHistory1).fieldHistoryId

    val fieldHistory2 = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = MEDICAL_DIET,
      valueRef = REF_DATA_CODE,
      createdAt = EXPECTED_CREATED_AT.plusMinutes(1),
      createdBy = USER1,
      prisonId = PRISON_ID,
    )
    fieldHistoryRepository.save(fieldHistory2).fieldHistoryId

    val fieldHistory3 = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = PERSONALISED_DIET,
      valueRef = REF_DATA_CODE,
      createdAt = EXPECTED_CREATED_AT,
      createdBy = USER1,
      prisonId = PRISON_ID,
    )
    val expectedFirstRowInQueryResults = fieldHistoryRepository.save(fieldHistory3).fieldHistoryId

    val fieldHistory4 = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = CATERING_INSTRUCTIONS,
      valueRef = REF_DATA_CODE,
      createdAt = UNEXPECTED_CREATED_AT_AFTER_TO,
      createdBy = USER1,
      prisonId = PRISON_ID,
    )
    fieldHistoryRepository.save(fieldHistory4).fieldHistoryId

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val results: SortedSet<FieldHistory> = fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(
      PRISONER_NUMBER,
      QUERY_FROM,
      QUERY_TO,
    )

    assertThat(results.size).isEqualTo(2)

    with(results.first as FieldHistory) {
      assertThat(fieldHistoryId).isEqualTo(expectedFirstRowInQueryResults)
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(field).isEqualTo(PERSONALISED_DIET)
      assertThat(valueInt).isNull()
      assertThat(valueRef).isEqualTo(REF_DATA_CODE)
      assertThat(createdAt).isEqualTo(EXPECTED_CREATED_AT)
      assertThat(createdBy).isEqualTo(USER1)
    }
  }

  @Test
  fun `prisoner not found`() {
    val fieldHistory1 = FieldHistory(
      prisonerNumber = PRISONER_NUMBER,
      field = FOOD_ALLERGY,
      valueRef = REF_DATA_CODE,
      createdAt = UNEXPECTED_CREATED_AT_BEFORE_FROM,
      createdBy = USER1,
      prisonId = PRISON_ID,
    )
    fieldHistoryRepository.save(fieldHistory1).fieldHistoryId

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val results: SortedSet<FieldHistory> = fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(
      UNKNOWN_PRISONER_NUMBER,
      QUERY_FROM,
      QUERY_TO,
    )

    assertThat(results.size).isEqualTo(0)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val UNKNOWN_PRISONER_NUMBER = "UNKNOWN_PRN"
    const val USER1 = "USER1"
    const val PRISON_ID = "STI"

    val UNEXPECTED_CREATED_AT_BEFORE_FROM = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.systemDefault())
    val EXPECTED_CREATED_AT = ZonedDateTime.of(2025, 1, 2, 23, 59, 59, 0, ZoneId.systemDefault())
    val UNEXPECTED_CREATED_AT_AFTER_TO = ZonedDateTime.of(2025, 1, 4, 12, 0, 0, 0, ZoneId.systemDefault())
    val QUERY_FROM = ZonedDateTime.of(2025, 1, 2, 0, 0, 0, 0, ZoneId.systemDefault())
    val QUERY_TO = ZonedDateTime.of(2025, 1, 3, 12, 0, 0, 0, ZoneId.systemDefault())

    val EXPECTED_FIRST_FIELD_HISTORY_ID = 4L

    val REF_DATA_DOMAIN = ReferenceDataDomain("FOOD_ALLERGY", "Food allergy", 0, ZonedDateTime.now(), "OMS_OWNER")
    val REF_DATA_CODE = ReferenceDataCode(
      id = "FOOD_ALLERGY_PEANUTS",
      domain = REF_DATA_DOMAIN,
      code = "PEANUTS",
      description = "Peanuts",
      listSequence = 9,
      createdAt = ZonedDateTime.now(),
      createdBy = "OMS_OWNER",
    )
    val INVALID_REF_DATA_CODE = ReferenceDataCode(
      id = "FOOD_ALLERGY_INVALID",
      domain = REF_DATA_DOMAIN,
      code = "INVALID",
      description = "INVALID",
      listSequence = 1,
      createdAt = ZonedDateTime.now(),
      createdBy = "testUser",
    )

    val NOW: ZonedDateTime = ZonedDateTime.now(clock)
  }
}
