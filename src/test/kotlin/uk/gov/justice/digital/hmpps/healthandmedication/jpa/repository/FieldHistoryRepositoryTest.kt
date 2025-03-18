package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

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

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val PRISON_ID = "STI"

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
