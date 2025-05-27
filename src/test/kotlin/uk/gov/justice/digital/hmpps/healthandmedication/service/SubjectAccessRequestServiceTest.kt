package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.CATERING_INSTRUCTIONS
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.MEDICAL_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.PERSONALISED_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.integration.TestBase
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergyHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergyHistoryItem
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.JsonObject
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementItem
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirementItem
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.SubjectAccessRequestFieldHistoryType
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.SubjectAccessRequestResponseDto
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.UNKNOWN_REFERENCE_DATA_DESCRIPTION
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class SubjectAccessRequestServiceTest {

  @Mock
  private lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Mock
  lateinit var fieldHistoryRepository: FieldHistoryRepository

  @InjectMocks
  private lateinit var subjectaccessRequestService: SubjectAccessRequestService

  @Test
  fun `can retrieve ordered SAR data for a prisoner when data is between two dates`() {
    val queryFrom = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
    val queryTo = ZonedDateTime.of(2025, 1, 4, 23, 59, 59, 999999999, ZoneId.systemDefault())

    val expectedCreatedAt = ZonedDateTime.of(2025, 1, 2, 23, 59, 59, 0, ZoneId.systemDefault())

    whenever(referenceDataCodeRepository.findById(FOOD_ALLERGY_CODE.id)).thenReturn(
      Optional.of(
        FOOD_ALLERGY_CODE,
      ),
    )

    whenever(referenceDataCodeRepository.findById(MEDICAL_DIET_CODE.id)).thenReturn(
      Optional.of(
        MEDICAL_DIET_CODE,
      ),
    )

    whenever(referenceDataCodeRepository.findById(PERSONALISED_DIET_CODE.id)).thenReturn(
      Optional.of(
        PERSONALISED_DIET_CODE,
      ),
    )

    whenever(fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)).thenReturn(

      sortedSetOf(
        FieldHistory(
          fieldHistoryId = 0,
          prisonerNumber = PRISONER_NUMBER,
          field = FOOD_ALLERGY,
          valueJson = JsonObject(FOOD_ALLERGY, FoodAllergyHistory(listOf(FoodAllergyHistoryItem("FOOD_ALLERGY_PEANUTS"), FoodAllergyHistoryItem("FOOD_ALLERGY_PEANUTS")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 1,
          prisonerNumber = PRISONER_NUMBER,
          field = MEDICAL_DIET,
          valueJson = JsonObject(MEDICAL_DIET, MedicalDietaryRequirementHistory(listOf(MedicalDietaryRequirementItem(MEDICAL_DIET_CODE.id, "Some other diet")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 2,
          prisonerNumber = PRISONER_NUMBER,
          field = PERSONALISED_DIET,
          valueJson = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory(listOf(PersonalisedDietaryRequirementItem(PERSONALISED_DIET_CODE.id, "Some other diet")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 3,
          prisonerNumber = PRISONER_NUMBER,
          field = CATERING_INSTRUCTIONS,
          valueString = "Some catering instructions",
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
      ),
    )

    val fromDate: LocalDate = LocalDate.parse("2025-01-01")
    val toDate: LocalDate = LocalDate.parse("2025-01-04")

    val result: HmppsSubjectAccessRequestContent? = subjectaccessRequestService.getPrisonContentFor(PRISONER_NUMBER, fromDate, toDate)

    assertThat(result).isEqualTo(
      HmppsSubjectAccessRequestContent(
        listOf(
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 3,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.CATERING_INSTRUCTIONS.description,
            fieldHistoryValue = "Some catering instructions",
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 2,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.PERSONALISED_DIET.description,
            fieldHistoryValue = listOf(PersonalisedDietaryRequirementItem(PERSONALISED_DIET_CODE.description, "Some other diet")),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 1,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.MEDICAL_DIET.description,
            fieldHistoryValue = listOf(MedicalDietaryRequirementItem(MEDICAL_DIET_CODE.description, "Some other diet")),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 0,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.FOOD_ALLERGY.description,
            fieldHistoryValue = listOf(FoodAllergyHistoryItem(FOOD_ALLERGY_CODE.description), FoodAllergyHistoryItem(FOOD_ALLERGY_CODE.description)),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
        ),
      ),
    )

    verify(fieldHistoryRepository).findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)
  }

  @Test
  fun `can retrieve ordered SAR data for a prisoner when data is partially before two dates`() {
    val queryFrom = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
    val queryTo = ZonedDateTime.of(2025, 1, 4, 23, 59, 59, 999999999, ZoneId.systemDefault())

    val expectedCreatedAt = ZonedDateTime.of(2025, 1, 2, 23, 59, 59, 0, ZoneId.systemDefault())
    val expectedCreatedAtLatestAvailable = ZonedDateTime.of(2024, 12, 31, 23, 59, 59, 0, ZoneId.systemDefault())

    whenever(referenceDataCodeRepository.findById(FOOD_ALLERGY_CODE.id)).thenReturn(
      Optional.of(
        FOOD_ALLERGY_CODE,
      ),
    )

    whenever(referenceDataCodeRepository.findById(MEDICAL_DIET_CODE.id)).thenReturn(
      Optional.of(
        MEDICAL_DIET_CODE,
      ),
    )

    whenever(referenceDataCodeRepository.findById(PERSONALISED_DIET_CODE.id)).thenReturn(
      Optional.of(
        PERSONALISED_DIET_CODE,
      ),
    )

    whenever(fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)).thenReturn(

      sortedSetOf(
        FieldHistory(
          fieldHistoryId = 0,
          prisonerNumber = PRISONER_NUMBER,
          field = FOOD_ALLERGY,
          valueJson = JsonObject(FOOD_ALLERGY, FoodAllergyHistory(listOf(FoodAllergyHistoryItem("FOOD_ALLERGY_PEANUTS"), FoodAllergyHistoryItem("FOOD_ALLERGY_PEANUTS")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAtLatestAvailable,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 1,
          prisonerNumber = PRISONER_NUMBER,
          field = MEDICAL_DIET,
          valueJson = JsonObject(MEDICAL_DIET, MedicalDietaryRequirementHistory(listOf(MedicalDietaryRequirementItem(MEDICAL_DIET_CODE.id, "Some other diet")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAtLatestAvailable,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 2,
          prisonerNumber = PRISONER_NUMBER,
          field = PERSONALISED_DIET,
          valueJson = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory(listOf(PersonalisedDietaryRequirementItem(PERSONALISED_DIET_CODE.id, "Some other diet")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 3,
          prisonerNumber = PRISONER_NUMBER,
          field = CATERING_INSTRUCTIONS,
          valueString = "Some catering instructions",
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
      ),
    )

    val fromDate: LocalDate = LocalDate.parse("2025-01-01")
    val toDate: LocalDate = LocalDate.parse("2025-01-04")

    val result: HmppsSubjectAccessRequestContent? = subjectaccessRequestService.getPrisonContentFor(PRISONER_NUMBER, fromDate, toDate)

    assertThat(result).isEqualTo(
      HmppsSubjectAccessRequestContent(
        listOf(
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 3,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.CATERING_INSTRUCTIONS.description,
            fieldHistoryValue = "Some catering instructions",
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 2,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.PERSONALISED_DIET.description,
            fieldHistoryValue = listOf(PersonalisedDietaryRequirementItem(PERSONALISED_DIET_CODE.description, "Some other diet")),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 1,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.MEDICAL_DIET.description,
            fieldHistoryValue = listOf(MedicalDietaryRequirementItem(MEDICAL_DIET_CODE.description, "Some other diet")),
            createdAt = expectedCreatedAtLatestAvailable,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 0,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.FOOD_ALLERGY.description,
            fieldHistoryValue = listOf(FoodAllergyHistoryItem(FOOD_ALLERGY_CODE.description), FoodAllergyHistoryItem(FOOD_ALLERGY_CODE.description)),
            createdAt = expectedCreatedAtLatestAvailable,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
        ),
      ),
    )

    verify(fieldHistoryRepository).findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)
  }

  @Test
  fun `can retrieve ordered SAR data for a prisoner when data is completely before two dates`() {
    val queryFrom = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
    val queryTo = ZonedDateTime.of(2025, 1, 4, 23, 59, 59, 999999999, ZoneId.systemDefault())

    val expectedCreatedAtLatestAvailable = ZonedDateTime.of(2024, 12, 31, 23, 59, 59, 0, ZoneId.systemDefault())

    whenever(referenceDataCodeRepository.findById(FOOD_ALLERGY_CODE.id)).thenReturn(
      Optional.of(
        FOOD_ALLERGY_CODE,
      ),
    )

    whenever(referenceDataCodeRepository.findById(MEDICAL_DIET_CODE.id)).thenReturn(
      Optional.of(
        MEDICAL_DIET_CODE,
      ),
    )

    whenever(referenceDataCodeRepository.findById(PERSONALISED_DIET_CODE.id)).thenReturn(
      Optional.of(
        PERSONALISED_DIET_CODE,
      ),
    )

    whenever(fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)).thenReturn(

      sortedSetOf(
        FieldHistory(
          fieldHistoryId = 0,
          prisonerNumber = PRISONER_NUMBER,
          field = FOOD_ALLERGY,
          valueJson = JsonObject(FOOD_ALLERGY, FoodAllergyHistory(listOf(FoodAllergyHistoryItem("FOOD_ALLERGY_PEANUTS"), FoodAllergyHistoryItem("FOOD_ALLERGY_PEANUTS")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAtLatestAvailable,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 1,
          prisonerNumber = PRISONER_NUMBER,
          field = MEDICAL_DIET,
          valueJson = JsonObject(MEDICAL_DIET, MedicalDietaryRequirementHistory(listOf(MedicalDietaryRequirementItem(MEDICAL_DIET_CODE.id, "Some other diet")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAtLatestAvailable,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 2,
          prisonerNumber = PRISONER_NUMBER,
          field = PERSONALISED_DIET,
          valueJson = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory(listOf(PersonalisedDietaryRequirementItem(PERSONALISED_DIET_CODE.id, "Some other diet")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAtLatestAvailable,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 3,
          prisonerNumber = PRISONER_NUMBER,
          field = CATERING_INSTRUCTIONS,
          valueString = "Some catering instructions",
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAtLatestAvailable,
          createdBy = USER1,
        ),
      ),
    )

    val fromDate: LocalDate = LocalDate.parse("2025-01-01")
    val toDate: LocalDate = LocalDate.parse("2025-01-04")

    val result: HmppsSubjectAccessRequestContent? = subjectaccessRequestService.getPrisonContentFor(PRISONER_NUMBER, fromDate, toDate)

    assertThat(result).isEqualTo(
      HmppsSubjectAccessRequestContent(
        listOf(
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 3,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.CATERING_INSTRUCTIONS.description,
            fieldHistoryValue = "Some catering instructions",
            createdAt = expectedCreatedAtLatestAvailable,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 2,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.PERSONALISED_DIET.description,
            fieldHistoryValue = listOf(PersonalisedDietaryRequirementItem(PERSONALISED_DIET_CODE.description, "Some other diet")),
            createdAt = expectedCreatedAtLatestAvailable,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 1,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.MEDICAL_DIET.description,
            fieldHistoryValue = listOf(MedicalDietaryRequirementItem(MEDICAL_DIET_CODE.description, "Some other diet")),
            createdAt = expectedCreatedAtLatestAvailable,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 0,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.FOOD_ALLERGY.description,
            fieldHistoryValue = listOf(FoodAllergyHistoryItem(FOOD_ALLERGY_CODE.description), FoodAllergyHistoryItem(FOOD_ALLERGY_CODE.description)),
            createdAt = expectedCreatedAtLatestAvailable,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
        ),
      ),
    )

    verify(fieldHistoryRepository).findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)
  }

  @Test
  fun `no data for a prisoner between two dates`() {
    val queryFrom = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
    val queryTo = ZonedDateTime.of(2025, 1, 4, 23, 59, 59, 999999999, ZoneId.systemDefault())

    whenever(fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)).thenReturn(null)

    val fromDate: LocalDate = LocalDate.parse("2025-01-01")
    val toDate: LocalDate = LocalDate.parse("2025-01-04")

    val result: HmppsSubjectAccessRequestContent? = subjectaccessRequestService.getPrisonContentFor(PRISONER_NUMBER, fromDate, toDate)

    assertThat(result).isNull()

    verify(fieldHistoryRepository).findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)
  }

  @Test
  fun `prisoner not found`() {
    val queryFrom = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
    val queryTo = ZonedDateTime.of(2025, 1, 4, 23, 59, 59, 999999999, ZoneId.systemDefault())

    whenever(fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)).thenReturn(null)

    val fromDate: LocalDate = LocalDate.parse("2025-01-01")
    val toDate: LocalDate = LocalDate.parse("2025-01-04")

    val result: HmppsSubjectAccessRequestContent? = subjectaccessRequestService.getPrisonContentFor(UNKNOWN_PRISONER_NUMBER, fromDate, toDate)

    assertThat(result).isNull()

    verify(fieldHistoryRepository).findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(UNKNOWN_PRISONER_NUMBER, queryFrom, queryTo)
  }

  @Test
  fun `null start date and null end date converted into 1970-01-01 00h 00m 00s and 3000-01-01 23h 59m 59s when querying data`() {
    val queryFrom = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
    val queryTo = ZonedDateTime.of(3000, 1, 1, 23, 59, 59, 999999999, ZoneId.systemDefault())

    whenever(fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)).thenReturn(null)

    val fromDate: LocalDate? = null
    val toDate: LocalDate? = null

    val result: HmppsSubjectAccessRequestContent? = subjectaccessRequestService.getPrisonContentFor(PRISONER_NUMBER, fromDate, toDate)

    assertThat(result).isNull()

    verify(fieldHistoryRepository).findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)
  }

  @Test
  fun `can tolerate misformed SAR data for a prisoner between two dates`() {
    val queryFrom = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
    val queryTo = ZonedDateTime.of(2025, 1, 4, 23, 59, 59, 999999999, ZoneId.systemDefault())

    val expectedCreatedAt = ZonedDateTime.of(2025, 1, 2, 23, 59, 59, 0, ZoneId.systemDefault())

    whenever(referenceDataCodeRepository.findById(FOOD_ALLERGY_CODE.id)).thenReturn(
      Optional.of(
        FOOD_ALLERGY_CODE,
      ),
    )

    whenever(referenceDataCodeRepository.findById(MEDICAL_DIET_CODE.id)).thenReturn(
      Optional.of(
        MEDICAL_DIET_CODE,
      ),
    )

    whenever(referenceDataCodeRepository.findById(PERSONALISED_DIET_CODE.id)).thenReturn(
      Optional.of(
        PERSONALISED_DIET_CODE,
      ),
    )

    whenever(fieldHistoryRepository.findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)).thenReturn(

      sortedSetOf(
        FieldHistory(
          fieldHistoryId = 1,
          prisonerNumber = PRISONER_NUMBER,
          field = FOOD_ALLERGY,
          valueJson = JsonObject(FOOD_ALLERGY, FoodAllergyHistory(listOf(FoodAllergyHistoryItem("bad reference code", "Some other diet"), FoodAllergyHistoryItem("FOOD_ALLERGY_PEANUTS")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 2,
          prisonerNumber = PRISONER_NUMBER,
          field = FOOD_ALLERGY,
          valueJson = JsonObject(FOOD_ALLERGY, FoodAllergyHistory()),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 3,
          prisonerNumber = PRISONER_NUMBER,
          field = FOOD_ALLERGY,
          valueJson = JsonObject(FOOD_ALLERGY, null),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 4,
          prisonerNumber = PRISONER_NUMBER,
          field = MEDICAL_DIET,
          valueJson = JsonObject(MEDICAL_DIET, MedicalDietaryRequirementHistory(listOf(MedicalDietaryRequirementItem("bad reference code", "Some other diet"), MedicalDietaryRequirementItem(MEDICAL_DIET_CODE.id, "Some other diet")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 5,
          prisonerNumber = PRISONER_NUMBER,
          field = MEDICAL_DIET,
          valueJson = JsonObject(MEDICAL_DIET, MedicalDietaryRequirementHistory()),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 6,
          prisonerNumber = PRISONER_NUMBER,
          field = MEDICAL_DIET,
          valueJson = JsonObject(MEDICAL_DIET, null),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 7,
          prisonerNumber = PRISONER_NUMBER,
          field = PERSONALISED_DIET,
          valueJson = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory(listOf(PersonalisedDietaryRequirementItem("bad reference code", "Some other diet"), PersonalisedDietaryRequirementItem(PERSONALISED_DIET_CODE.id, "Some other diet")))),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 8,
          prisonerNumber = PRISONER_NUMBER,
          field = PERSONALISED_DIET,
          valueJson = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory()),
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 9,
          prisonerNumber = PRISONER_NUMBER,
          field = PERSONALISED_DIET,
          valueJson = null,
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
        FieldHistory(
          fieldHistoryId = 10,
          prisonerNumber = PRISONER_NUMBER,
          field = CATERING_INSTRUCTIONS,
          valueString = null,
          prisonId = PRISON_ID,
          createdAt = expectedCreatedAt,
          createdBy = USER1,
        ),
      ),
    )

    val fromDate: LocalDate = LocalDate.parse("2025-01-01")
    val toDate: LocalDate = LocalDate.parse("2025-01-04")

    val result: HmppsSubjectAccessRequestContent? = subjectaccessRequestService.getPrisonContentFor(PRISONER_NUMBER, fromDate, toDate)

    assertThat(result).isEqualTo(
      HmppsSubjectAccessRequestContent(
        listOf(
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 10,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.CATERING_INSTRUCTIONS.description,
            fieldHistoryValue = null,
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 9,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.PERSONALISED_DIET.description,
            fieldHistoryValue = null,
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 8,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.PERSONALISED_DIET.description,
            fieldHistoryValue = listOf<PersonalisedDietaryRequirementItem>(),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 7,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.PERSONALISED_DIET.description,
            fieldHistoryValue = listOf(PersonalisedDietaryRequirementItem(UNKNOWN_REFERENCE_DATA_DESCRIPTION, "Some other diet"), PersonalisedDietaryRequirementItem(PERSONALISED_DIET_CODE.description, "Some other diet")),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 6,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.MEDICAL_DIET.description,
            fieldHistoryValue = null,
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 5,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.MEDICAL_DIET.description,
            fieldHistoryValue = listOf<MedicalDietaryRequirementItem>(),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 4,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.MEDICAL_DIET.description,
            fieldHistoryValue = listOf(MedicalDietaryRequirementItem(UNKNOWN_REFERENCE_DATA_DESCRIPTION, "Some other diet"), MedicalDietaryRequirementItem(MEDICAL_DIET_CODE.description, "Some other diet")),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 3,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.FOOD_ALLERGY.description,
            fieldHistoryValue = null,
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 2,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.FOOD_ALLERGY.description,
            fieldHistoryValue = listOf<FoodAllergyHistoryItem>(),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
          SubjectAccessRequestResponseDto(
            fieldHistoryId = 1,
            prisonerNumber = PRISONER_NUMBER,
            fieldHistoryType = SubjectAccessRequestFieldHistoryType.FOOD_ALLERGY.description,
            fieldHistoryValue = listOf(FoodAllergyHistoryItem(UNKNOWN_REFERENCE_DATA_DESCRIPTION, "Some other diet"), FoodAllergyHistoryItem(FOOD_ALLERGY_CODE.description)),
            createdAt = expectedCreatedAt,
            createdBy = USER1,
            prisonId = PRISON_ID,
            mergedAt = null,
            mergedFrom = null,
          ),
        ),
      ),
    )

    verify(fieldHistoryRepository).findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(PRISONER_NUMBER, queryFrom, queryTo)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val UNKNOWN_PRISONER_NUMBER = "UNKNOWN_PRN"
    const val USER1 = "USER1"
    const val PRISON_ID = "STI"

    val NOW: ZonedDateTime = ZonedDateTime.now(TestBase.clock)

    val FOOD_ALLERGY_CODE = ReferenceDataCode(
      id = "FOOD_ALLERGY_PEANUTS",
      code = "PEANUTS",
      createdBy = USER1,
      createdAt = NOW,
      description = "Peanuts",
      listSequence = 9,
      domain = ReferenceDataDomain(
        code = "FOOD_ALLERGY",
        createdBy = USER1,
        createdAt = NOW,
        listSequence = 0,
        description = "Food allergy",
      ),
    )

    val MEDICAL_DIET_CODE = ReferenceDataCode(
      id = "MEDICAL_DIET_COELIAC",
      code = "COELIAC",
      createdBy = USER1,
      createdAt = NOW,
      description = "Coeliac",
      listSequence = 0,
      domain = ReferenceDataDomain(
        code = "MEDICAL_DIET",
        createdBy = USER1,
        createdAt = NOW,
        listSequence = 0,
        description = "Medical diet",
      ),
    )

    val PERSONALISED_DIET_CODE = ReferenceDataCode(
      id = "PERSONALISED_DIET_VEGAN",
      code = "VEGAN",
      createdBy = USER1,
      createdAt = NOW,
      description = "Vegan",
      listSequence = 0,
      domain = ReferenceDataDomain(
        code = "PERSONALISED_DIET",
        createdBy = USER1,
        createdAt = NOW,
        listSequence = 0,
        description = "Personalised diet",
      ),
    )
  }
}
