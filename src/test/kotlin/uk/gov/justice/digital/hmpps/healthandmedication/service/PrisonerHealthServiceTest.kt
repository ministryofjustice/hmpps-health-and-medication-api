package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.healthandmedication.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.healthandmedication.dto.request.PrisonerHealthUpdateRequest
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.healthandmedication.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.MEDICAL_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergies
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.JsonObject
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirements
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.PrisonerHealthRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.healthandmedication.utils.AuthenticationFacade
import java.time.Clock
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class PrisonerHealthServiceTest {
  @Mock
  lateinit var prisonerHealthRepository: PrisonerHealthRepository

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Mock
  lateinit var authenticationFacade: AuthenticationFacade

  @Spy
  val clock: Clock? = Clock.fixed(NOW.toInstant(), NOW.zone)

  @InjectMocks
  lateinit var underTest: PrisonerHealthService

  private val savedPrisonerHealth = argumentCaptor<PrisonerHealth>()

  @BeforeEach
  fun beforeEach() {
    whenever(referenceDataCodeRepository.findById(FOOD_REFERENCE_DATA_CODE_ID)).thenReturn(Optional.of(EGG_ALLERGY))
    whenever(referenceDataCodeRepository.findById(LOW_FAT_REFERENCE_DATA_CODE_ID)).thenReturn(
      Optional.of(
        LOW_FAT_REFERENCE_DATA_CODE,
      ),
    )
    whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
  }

  @Test
  fun `health data not found`() {
    whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

    val result = underTest.getHealth(PRISONER_NUMBER)
    assertThat(result).isNull()
  }

  @Test
  fun `prison health data is found`() {
    whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(
      Optional.of(
        PrisonerHealth(
          prisonerNumber = PRISONER_NUMBER,
          medicalDietaryRequirements = mutableSetOf(
            LOW_FAT_DIET_REQUIREMENT,
          ),
          foodAllergies = mutableSetOf(
            EGG_FOOD_ALLERGY,
          ),
          fieldMetadata = mutableMapOf(
            MEDICAL_DIET to FieldMetadata(
              PRISONER_NUMBER,
              MEDICAL_DIET,
              NOW,
              USER1,
            ),
            FOOD_ALLERGY to FieldMetadata(
              PRISONER_NUMBER,
              MEDICAL_DIET,
              NOW,
              USER1,
            ),
          ),
        ),
      ),
    )

    val result = underTest.getHealth(PRISONER_NUMBER)

    assertThat(result).isEqualTo(
      HealthDto(
        medicalDietaryRequirements = ValueWithMetadata(
          listOf(
            ReferenceDataSimpleDto(
              id = LOW_FAT_REFERENCE_DATA_CODE_ID,
              description = LOW_FAT_DIET_REQUIREMENT.dietaryRequirement.description,
              listSequence = LOW_FAT_DIET_REQUIREMENT.dietaryRequirement.listSequence,
              isActive = true,
            ),
          ),
          NOW,
          USER1,
        ),
        foodAllergies = ValueWithMetadata(
          listOf(
            ReferenceDataSimpleDto(
              id = EGG_ALLERGY.id,
              description = EGG_ALLERGY.description,
              listSequence = EGG_ALLERGY.listSequence,
              isActive = true,
            ),
          ),
          NOW,
          USER1,
        ),
      ),
    )
  }

  @Nested
  inner class CreateOrUpdatePrisonerHealth {

    @BeforeEach
    fun beforeEach() {
      whenever(prisonerHealthRepository.save(savedPrisonerHealth.capture())).thenAnswer { savedPrisonerHealth.firstValue }
    }

    @Test
    fun `creating new health data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
        PRISONER_SEARCH_RESPONSE,
      )

      whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(
        underTest.createOrUpdate(
          PRISONER_NUMBER,
          HEALTH_UPDATE_REQUEST,
        ),
      ).isEqualTo(
        HealthDto(
          foodAllergies = ValueWithMetadata(listOf(EGG_ALLERGY.toSimpleDto()), NOW, USER1),
          medicalDietaryRequirements = ValueWithMetadata(listOf(LOW_FAT_REFERENCE_DATA_CODE.toSimpleDto()), NOW, USER1),
        ),
      )

      with(savedPrisonerHealth.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(foodAllergies).containsAll(listOf(EGG_FOOD_ALLERGY))
        assertThat(medicalDietaryRequirements).containsAll(listOf(LOW_FAT_DIET_REQUIREMENT))

        expectFieldHistory(
          MEDICAL_DIET,
          fieldHistory,
          HistoryComparison(
            value = JsonObject(
              field = MEDICAL_DIET,
              value = MedicalDietaryRequirements(medicalDietaryRequirements = listOf(LOW_FAT_REFERENCE_DATA_CODE_ID)),
            ),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
        expectFieldHistory(
          FOOD_ALLERGY,
          fieldHistory,
          HistoryComparison(
            value = JsonObject(
              field = FOOD_ALLERGY,
              value = FoodAllergies(listOf(EGG_ALLERGY.id)),
            ),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }
    }

    @Test
    fun `updating health data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
        PRISONER_SEARCH_RESPONSE,
      )

      whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PrisonerHealth(
            prisonerNumber = PRISONER_NUMBER,
            foodAllergies = mutableSetOf(EGG_FOOD_ALLERGY),
            medicalDietaryRequirements = mutableSetOf(LOW_FAT_DIET_REQUIREMENT),
          ).also { it.updateFieldHistory(lastModifiedAt = NOW.minusDays(1), lastModifiedBy = USER2) },
        ),
      )

      assertThat(underTest.createOrUpdate(PRISONER_NUMBER, HEALTH_UPDATE_REQUEST_WITH_NULL)).isEqualTo(
        HealthDto(
          foodAllergies = ValueWithMetadata(emptyList(), NOW, USER1),
          medicalDietaryRequirements = ValueWithMetadata(emptyList(), NOW, USER1),
        ),
      )

      fun <T> firstHistory(value: T): HistoryComparison<T> = HistoryComparison(
        value = value,
        createdAt = NOW.minusDays(1),
        createdBy = USER2,
      )

      fun <T> secondHistory(value: T): HistoryComparison<T> = HistoryComparison(
        value = value,
        createdAt = NOW,
        createdBy = USER1,
      )

      with(savedPrisonerHealth.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(foodAllergies).isEqualTo(mutableSetOf<FoodAllergy>())
        assertThat(medicalDietaryRequirements).isEqualTo(mutableSetOf<MedicalDietaryRequirement>())

        expectFieldHistory(
          MEDICAL_DIET,
          fieldHistory,
          firstHistory(
            JsonObject(
              field = MEDICAL_DIET,
              value = MedicalDietaryRequirements(medicalDietaryRequirements = listOf(LOW_FAT_REFERENCE_DATA_CODE_ID)),
            ),
          ),
          secondHistory(
            JsonObject(
              field = MEDICAL_DIET,
              value = MedicalDietaryRequirements(medicalDietaryRequirements = emptyList<String>()),
            ),
          ),
        )

        expectFieldHistory(
          FOOD_ALLERGY,
          fieldHistory,
          firstHistory(
            JsonObject(
              field = FOOD_ALLERGY,
              value = FoodAllergies(listOf(EGG_ALLERGY.id)),
            ),
          ),
          secondHistory(
            JsonObject(
              field = FOOD_ALLERGY,
              value = FoodAllergies(emptyList<String>()),
            ),
          ),
        )
      }
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW = ZonedDateTime.now()

    val REFERENCE_DATA_CODE_ID = "EXAMPLE_CODE"
    val REFERENCE_DATA_CODE_DESCRPTION = "Example code"
    val REFERENCE_DATA_LIST_SEQUENCE = 0

    val FOOD_REFERENCE_DATA_CODE_ID = "FOOD_EXAMPLE_CODE"
    val FOOD_REFERENCE_DATA_CODE = "FOOD_CODE"
    val FOOD_REFERENCE_DATA_CODE_DESCRPTION = "Example food code"
    val FOOD_REFERENCE_DATA_LIST_SEQUENCE = 0
    val FOOD_REFERENCE_DATA_DOMAIN_CODE = "FOOD_EXAMPLE"
    val FOOD_REFERENCE_DATA_DOMAIN_DESCRIPTION = "Food Example"

    val EGG_ALLERGY = ReferenceDataCode(
      id = FOOD_REFERENCE_DATA_CODE_ID,
      code = FOOD_REFERENCE_DATA_CODE,
      createdBy = USER1,
      createdAt = NOW,
      description = FOOD_REFERENCE_DATA_CODE_DESCRPTION,
      listSequence = FOOD_REFERENCE_DATA_LIST_SEQUENCE,
      domain = ReferenceDataDomain(
        code = FOOD_REFERENCE_DATA_DOMAIN_CODE,
        createdBy = USER1,
        createdAt = NOW,
        listSequence = FOOD_REFERENCE_DATA_LIST_SEQUENCE,
        description = FOOD_REFERENCE_DATA_DOMAIN_DESCRIPTION,
      ),
    )

    val EGG_FOOD_ALLERGY = FoodAllergy(prisonerNumber = PRISONER_NUMBER, allergy = EGG_ALLERGY)

    val LOW_FAT_REFERENCE_DATA_CODE_ID = "MEDICAL_DIET_LOW_FAT"
    val LOW_FAT_REFERENCE_DATA_CODE = ReferenceDataCode(
      id = LOW_FAT_REFERENCE_DATA_CODE_ID,
      code = "LOW_FAT",
      createdBy = USER1,
      createdAt = NOW,
      description = "Example medical diet code",
      listSequence = 0,
      domain = ReferenceDataDomain(
        code = "MEDICAL_DIET",
        createdBy = USER1,
        createdAt = NOW,
        listSequence = 0,
        description = "Example medical diet domain",
      ),
    )

    val LOW_FAT_DIET_REQUIREMENT = MedicalDietaryRequirement(
      prisonerNumber = PRISONER_NUMBER,
      dietaryRequirement = LOW_FAT_REFERENCE_DATA_CODE,
    )

    val PRISONER_SEARCH_RESPONSE = PrisonerDto(PRISONER_NUMBER)

    val attributes = mutableMapOf<String, Any?>(
      Pair("foodAllergies", listOf(FOOD_REFERENCE_DATA_CODE_ID)),
      Pair("medicalDietaryRequirements", listOf(LOW_FAT_REFERENCE_DATA_CODE_ID)),
    )

    val HEALTH_UPDATE_REQUEST = PrisonerHealthUpdateRequest(attributes)

    val attributes_undefined = mutableMapOf<String, Any?>(
      Pair("foodAllergies", emptyList<String>()),
      Pair("medicalDietaryRequirements", emptyList<String>()),
    )
    val HEALTH_UPDATE_REQUEST_WITH_NULL = PrisonerHealthUpdateRequest(attributes_undefined)
  }
}
