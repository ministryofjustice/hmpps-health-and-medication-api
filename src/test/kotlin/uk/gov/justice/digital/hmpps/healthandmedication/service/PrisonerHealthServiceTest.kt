package uk.gov.justice.digital.hmpps.healthandmedication.service

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatus.Y
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatusUpdate
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.response.PrisonerDto
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.MEDICAL_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.PERSONALISED_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.CateringInstructions
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergyHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.JsonObject
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.PrisonerHealthRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.ReferenceDataValue
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.HealthAndMedicationForPrisonRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.HealthAndMedicationRequestFilters
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.PageMeta
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.ReferenceDataIdSelection
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.UpdateDietAndAllergyRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.UpdateSmokerStatusRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.DietAndAllergyResponse
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationFilter
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationFiltersResponse
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationForPrisonDto
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationForPrisonResponse
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationResponse
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.ReferenceDataSelection
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.healthandmedication.utils.AuthenticationFacade
import java.time.Clock
import java.time.ZonedDateTime
import java.util.Optional
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class PrisonerHealthServiceTest {
  @Mock
  lateinit var prisonerHealthRepository: PrisonerHealthRepository

  @Mock
  lateinit var prisonApiClient: PrisonApiClient

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
    whenever(authenticationFacade.getUserOrSystemInContext()).thenReturn(USER1)
    whenever(referenceDataCodeRepository.findById(SMOKER_CODE.id)).thenReturn(Optional.of(SMOKER_CODE))
    whenever(referenceDataCodeRepository.findById(FOOD_ALLERGY_PEANUTS_CODE.id)).thenReturn(
      Optional.of(
        FOOD_ALLERGY_PEANUTS_CODE,
      ),
    )
    whenever(referenceDataCodeRepository.findById(MEDICAL_DIET_COELIAC_CODE.id)).thenReturn(
      Optional.of(
        MEDICAL_DIET_COELIAC_CODE,
      ),
    )
    whenever(referenceDataCodeRepository.findById(PERSONALISED_DIET_VEGAN_CODE.id)).thenReturn(
      Optional.of(
        PERSONALISED_DIET_VEGAN_CODE,
      ),
    )
  }

  @Nested
  inner class GetPrisonerHealth {
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
            foodAllergies = mutableSetOf(FOOD_ALLERGY_PEANUTS),
            medicalDietaryRequirements = mutableSetOf(MEDICAL_DIET_COELIAC),
            personalisedDietaryRequirements = mutableSetOf(PERSONALISED_DIET_VEGAN),

            fieldMetadata = mutableMapOf(
              FOOD_ALLERGY to FieldMetadata(
                prisonerNumber = PRISONER_NUMBER,
                field = FOOD_ALLERGY,
                lastModifiedAt = NOW,
                lastModifiedBy = USER1,
                lastModifiedPrisonId = "STI",
              ),
              MEDICAL_DIET to FieldMetadata(
                prisonerNumber = PRISONER_NUMBER,
                field = MEDICAL_DIET,
                lastModifiedAt = NOW,
                lastModifiedBy = USER1,
                lastModifiedPrisonId = "STI",
              ),
              PERSONALISED_DIET to FieldMetadata(
                prisonerNumber = PRISONER_NUMBER,
                field = PERSONALISED_DIET,
                lastModifiedAt = NOW,
                lastModifiedBy = USER1,
                lastModifiedPrisonId = "STI",
              ),
            ),
          ),
        ),
      )

      val result = underTest.getHealth(PRISONER_NUMBER)

      assertThat(result).isEqualTo(
        HealthAndMedicationResponse(
          dietAndAllergy = DietAndAllergyResponse(
            foodAllergies = ValueWithMetadata(
              listOf(
                ReferenceDataSelection(
                  ReferenceDataValue(
                    id = FOOD_ALLERGY_PEANUTS_CODE.id,
                    code = FOOD_ALLERGY_PEANUTS_CODE.code,
                    description = FOOD_ALLERGY_PEANUTS_CODE.description,
                  ),
                ),
              ),
              NOW,
              USER1,
              PRISON_ID,
            ),
            medicalDietaryRequirements = ValueWithMetadata(
              listOf(
                ReferenceDataSelection(
                  ReferenceDataValue(
                    id = MEDICAL_DIET_COELIAC_CODE.id,
                    code = MEDICAL_DIET_COELIAC_CODE.code,
                    description = MEDICAL_DIET_COELIAC_CODE.description,
                  ),
                ),
              ),
              NOW,
              USER1,
              PRISON_ID,
            ),
            personalisedDietaryRequirements = ValueWithMetadata(
              listOf(
                ReferenceDataSelection(
                  ReferenceDataValue(
                    id = PERSONALISED_DIET_VEGAN_CODE.id,
                    code = PERSONALISED_DIET_VEGAN_CODE.code,
                    description = PERSONALISED_DIET_VEGAN_CODE.description,
                  ),
                ),
              ),
              NOW,
              USER1,
              PRISON_ID,
            ),
          ),
        ),
      )
    }
  }

  @Nested
  inner class CreateOrUpdatePrisonerHealth {

    @BeforeEach
    fun beforeEach() {
      whenever(prisonerHealthRepository.save(savedPrisonerHealth.capture())).thenAnswer { savedPrisonerHealth.firstValue }
    }

    @Test
    fun `creating new health data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER))
        .thenReturn(PRISONER_SEARCH_RESPONSE)

      whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThat(
        underTest.updateDietAndAllergyData(
          PRISONER_NUMBER,
          DIET_AND_ALLERGY_UPDATE_REQUEST,
        ),
      ).isEqualTo(
        DietAndAllergyResponse(
          foodAllergies = ValueWithMetadata(
            listOf(ReferenceDataSelection(FOOD_ALLERGY_PEANUTS_CODE.toSimpleDto())),
            NOW,
            USER1,
            PRISON_ID,
          ),
          medicalDietaryRequirements = ValueWithMetadata(
            listOf(ReferenceDataSelection(MEDICAL_DIET_COELIAC_CODE.toSimpleDto())),
            NOW,
            USER1,
            PRISON_ID,
          ),
          personalisedDietaryRequirements = ValueWithMetadata(
            listOf(ReferenceDataSelection(PERSONALISED_DIET_VEGAN_CODE.toSimpleDto())),
            NOW,
            USER1,
            PRISON_ID,
          ),
          cateringInstructions = ValueWithMetadata(
            "Some catering instructions.",
            NOW,
            USER1,
            PRISON_ID,
          ),
        ),
      )

      with(savedPrisonerHealth.firstValue) {
        assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
        assertThat(foodAllergies).containsAll(listOf(FOOD_ALLERGY_PEANUTS))
        assertThat(medicalDietaryRequirements).containsAll(listOf(MEDICAL_DIET_COELIAC))
        assertThat(personalisedDietaryRequirements).containsAll(listOf(PERSONALISED_DIET_VEGAN))

        expectFieldHistory(
          FOOD_ALLERGY,
          fieldHistory,
          HistoryComparison(
            value = JsonObject(
              field = FOOD_ALLERGY,
              value = FoodAllergyHistory(FOOD_ALLERGY_PEANUTS_CODE.id),
            ),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )

        expectFieldHistory(
          MEDICAL_DIET,
          fieldHistory,
          HistoryComparison(
            value = JsonObject(
              field = MEDICAL_DIET,
              value = MedicalDietaryRequirementHistory(MEDICAL_DIET_COELIAC_CODE.id),
            ),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )

        expectFieldHistory(
          PERSONALISED_DIET,
          fieldHistory,
          HistoryComparison(
            value = JsonObject(
              field = PERSONALISED_DIET,
              value = PersonalisedDietaryRequirementHistory(PERSONALISED_DIET_VEGAN_CODE.id),
            ),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }
    }

    @Test
    fun `updating health data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER))
        .thenReturn(PRISONER_SEARCH_RESPONSE)

      whenever(prisonerHealthRepository.findById(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          PrisonerHealth(
            prisonerNumber = PRISONER_NUMBER,
            foodAllergies = mutableSetOf(FOOD_ALLERGY_PEANUTS),
            medicalDietaryRequirements = mutableSetOf(MEDICAL_DIET_COELIAC),
            personalisedDietaryRequirements = mutableSetOf(PERSONALISED_DIET_VEGAN),
            cateringInstructions = CateringInstructions(PRISONER_NUMBER, "outdated instructions"),
          ).also {
            it.updateFieldHistory(
              lastModifiedAt = NOW.minusDays(1),
              lastModifiedBy = USER2,
              lastModifiedPrisonId = "KMI",
            )
          },
        ),
      )

      assertThat(underTest.updateDietAndAllergyData(PRISONER_NUMBER, EMPTY_DIET_AND_ALLERGY_UPDATE_REQUEST)).isEqualTo(
        DietAndAllergyResponse(
          foodAllergies = ValueWithMetadata(emptyList(), NOW, USER1, PRISON_ID),
          medicalDietaryRequirements = ValueWithMetadata(emptyList(), NOW, USER1, PRISON_ID),
          personalisedDietaryRequirements = ValueWithMetadata(emptyList(), NOW, USER1, PRISON_ID),
          cateringInstructions = ValueWithMetadata(null, NOW, USER1, PRISON_ID),
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
        assertThat(personalisedDietaryRequirements).isEqualTo(mutableSetOf<PersonalisedDietaryRequirement>())

        expectFieldHistory(
          FOOD_ALLERGY,
          fieldHistory,
          firstHistory(
            JsonObject(
              field = FOOD_ALLERGY,
              value = FoodAllergyHistory(FOOD_ALLERGY_PEANUTS_CODE.id),
            ),
          ),
          secondHistory(
            JsonObject(
              field = FOOD_ALLERGY,
              value = FoodAllergyHistory(),
            ),
          ),
        )

        expectFieldHistory(
          MEDICAL_DIET,
          fieldHistory,
          firstHistory(
            JsonObject(
              field = MEDICAL_DIET,
              value = MedicalDietaryRequirementHistory(MEDICAL_DIET_COELIAC_CODE.id),
            ),
          ),
          secondHistory(
            JsonObject(
              field = MEDICAL_DIET,
              value = MedicalDietaryRequirementHistory(),
            ),
          ),
        )

        expectFieldHistory(
          PERSONALISED_DIET,
          fieldHistory,
          firstHistory(
            JsonObject(
              field = PERSONALISED_DIET,
              value = PersonalisedDietaryRequirementHistory(PERSONALISED_DIET_VEGAN_CODE.id),
            ),
          ),
          secondHistory(
            JsonObject(
              field = PERSONALISED_DIET,
              value = PersonalisedDietaryRequirementHistory(),
            ),
          ),
        )
      }
    }
  }

  @Nested
  inner class GetHealthForPrison {
    @Nested
    inner class PrisonerSearchRequests {
      @BeforeEach
      fun beforeEach() {
        whenever(prisonerSearchClient.getPrisonersForPrison(PRISON_ID))
          .thenReturn(emptyList())
      }

      @Test
      fun `getting health for prison without sorting`() {
        underTest.getHealthForPrison(PRISON_ID, HealthAndMedicationForPrisonRequest(1, 10))
        verify(prisonerSearchClient).getPrisonersForPrison(PRISON_ID)
      }

      @ParameterizedTest
      @ValueSource(strings = ["asc", "desc"])
      fun `getting health for prison sorting by prisonerName`(direction: String) {
        underTest.getHealthForPrison(
          PRISON_ID,
          HealthAndMedicationForPrisonRequest(1, 10, sort = "prisonerName,$direction"),
        )

        verify(prisonerSearchClient).getPrisonersForPrison(PRISON_ID, "lastName,firstName,$direction")
      }

      @ParameterizedTest
      @ValueSource(strings = ["asc", "desc"])
      fun `getting health for prison sorting by location`(direction: String) {
        underTest.getHealthForPrison(
          PRISON_ID,
          HealthAndMedicationForPrisonRequest(1, 10, sort = "location,$direction"),
        )
        verify(prisonerSearchClient).getPrisonersForPrison(PRISON_ID, "cellLocation,$direction")
      }

      @Test
      fun `it throws a validation exception when an invalid sort field is passed`() {
        assertThrows<ValidationException> {
          underTest.getHealthForPrison(PRISON_ID, HealthAndMedicationForPrisonRequest(1, 10, "invalid,sort"))
        }
      }
    }

    @Nested
    inner class PrisonerHealthFetching {
      private val secondPrisonerNumber = "A1234BB"
      private val secondPrisonerHealth = PrisonerHealth(
        prisonerNumber = secondPrisonerNumber,
        foodAllergies = mutableSetOf(FOOD_ALLERGY_PEANUTS, FOOD_ALLERGY_OTHER),
        medicalDietaryRequirements = mutableSetOf(MEDICAL_DIET_COELIAC, MEDICAL_DIET_OTHER),
        personalisedDietaryRequirements = mutableSetOf(PERSONALISED_DIET_VEGAN, PERSONALISED_DIET_OTHER),
      )

      private val firstPrisonerHealthResponse = HealthAndMedicationForPrisonDto(
        prisonerNumber = PRISONER_NUMBER,
        firstName = PRISONER_FIRST_NAME,
        lastName = PRISONER_LAST_NAME,
        location = PRISONER_LOCATION,
        health = PRISONER_HEALTH.toHealthDto(),
      )
      val secondPrisonerHealthResponse = HealthAndMedicationForPrisonDto(
        prisonerNumber = secondPrisonerNumber,
        firstName = PRISONER_FIRST_NAME,
        lastName = PRISONER_LAST_NAME,
        location = PRISONER_LOCATION,
        health = secondPrisonerHealth.toHealthDto(),
      )

      @BeforeEach
      fun beforeEach() {
        whenever(prisonerSearchClient.getPrisonersForPrison(PRISON_ID))
          .thenReturn(
            listOf(
              PRISONER_SEARCH_RESPONSE,
              PrisonerDto(
                prisonerNumber = secondPrisonerNumber,
                prisonId = PRISON_ID,
                firstName = PRISONER_FIRST_NAME,
                lastName = PRISONER_LAST_NAME,
                cellLocation = PRISONER_LOCATION,
              ),
            ),
          )

        whenever(
          prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(
            mutableListOf(
              PRISONER_NUMBER,
              secondPrisonerNumber,
            ),
          ),
        ).thenReturn(
          listOf(
            PRISONER_HEALTH,
            secondPrisonerHealth,
          ),
        )
      }

      @Test
      fun `it doesnt crash with 0 prisoners`() {
        whenever(
          prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(
            mutableListOf(
              PRISONER_NUMBER,
              secondPrisonerNumber,
            ),
          ),
        ).thenReturn(emptyList())

        assertThat(
          underTest.getHealthForPrison(PRISON_ID, HealthAndMedicationForPrisonRequest(1, 10)),
        ).isEqualTo(
          HealthAndMedicationForPrisonResponse(
            content = emptyList(),
            metadata = PageMeta(
              first = true,
              last = true,
              numberOfElements = 0,
              offset = 0,
              pageNumber = 1,
              size = 10,
              totalElements = 0,
              totalPages = 1,
            ),
          ),
        )
      }

      @Test
      fun `it fetches the health information for the returned prisoners with no filters specified`() {
        assertThat(
          underTest.getHealthForPrison(PRISON_ID, HealthAndMedicationForPrisonRequest(1, 10)),
        ).isEqualTo(
          HealthAndMedicationForPrisonResponse(
            content = listOf(
              firstPrisonerHealthResponse,
              secondPrisonerHealthResponse,
            ),
            metadata = PageMeta(
              first = true,
              last = true,
              numberOfElements = 2,
              offset = 0,
              pageNumber = 1,
              size = 10,
              totalElements = 2,
              totalPages = 1,
            ),
          ),
        )
      }

      @Test
      fun `Returns no results if the provided page number is too high`() {
        assertThat(
          underTest.getHealthForPrison(PRISON_ID, HealthAndMedicationForPrisonRequest(2, 10)),
        ).isEqualTo(
          HealthAndMedicationForPrisonResponse(
            content = listOf(),
            metadata = PageMeta(
              first = false,
              last = true,
              numberOfElements = 0,
              offset = 10,
              pageNumber = 2,
              size = 10,
              totalElements = 2,
              totalPages = 1,
            ),
          ),
        )
      }

      @Nested
      inner class Filtering {
        @Test
        fun `it returns an empty response if no filters match`() {
          assertThat(
            underTest.getHealthForPrison(
              PRISON_ID,
              HealthAndMedicationForPrisonRequest(
                page = 1,
                size = 10,
                filters = HealthAndMedicationRequestFilters(foodAllergies = setOf("NO_MATCH")),
              ),
            ),
          ).isEqualTo(
            HealthAndMedicationForPrisonResponse(
              content = listOf(),
              metadata = PageMeta(
                first = true,
                last = true,
                numberOfElements = 0,
                offset = 0,
                pageNumber = 1,
                size = 10,
                totalElements = 0,
                totalPages = 1,
              ),
            ),
          )
        }

        @ParameterizedTest
        @MethodSource("uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerHealthServiceTest#singleHealthAndMedicationFilters")
        fun `it returns results matching a single provided filter`(filters: HealthAndMedicationRequestFilters) {
          assertThat(
            underTest.getHealthForPrison(
              PRISON_ID,
              HealthAndMedicationForPrisonRequest(
                page = 1,
                size = 10,
                filters = filters,
              ),
            ),
          ).isEqualTo(
            HealthAndMedicationForPrisonResponse(
              content = listOf(secondPrisonerHealthResponse),
              metadata = PageMeta(
                first = true,
                last = true,
                numberOfElements = 1,
                offset = 0,
                pageNumber = 1,
                size = 10,
                totalElements = 1,
                totalPages = 1,
              ),
            ),
          )
        }

        @Test
        fun `returns results matching any filter when multiple filters are provided`() {
          assertThat(
            underTest.getHealthForPrison(
              PRISON_ID,
              HealthAndMedicationForPrisonRequest(
                page = 1,
                size = 10,
                filters = HealthAndMedicationRequestFilters(
                  foodAllergies = setOf("PEANUTS", "TREE_NUTS"),
                  personalisedDietaryRequirements = setOf("KOSHER"),
                  medicalDietaryRequirements = setOf("NUTRIENT_DEFICIENCY"),
                ),
              ),
            ),
          ).isEqualTo(
            HealthAndMedicationForPrisonResponse(
              content = listOf(
                firstPrisonerHealthResponse,
                secondPrisonerHealthResponse,
              ),
              metadata = PageMeta(
                first = true,
                last = true,
                numberOfElements = 2,
                offset = 0,
                pageNumber = 1,
                size = 10,
                totalElements = 2,
                totalPages = 1,
              ),
            ),
          )
        }
      }
    }
  }

  @Nested
  inner class GetHealthFiltersForPrison {
    @BeforeEach
    fun beforeEach() {
      whenever(prisonerSearchClient.getPrisonersForPrison(PRISON_ID))
        .thenReturn(listOf(PRISONER_SEARCH_RESPONSE))
    }

    @Test
    fun `Calculates available filters based on existing health and medication data for prison`() {
      whenever(
        prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(
          mutableListOf(
            PRISONER_NUMBER,
          ),
        ),
      ).thenReturn(
        listOf(
          PRISONER_HEALTH,
          PrisonerHealth(
            prisonerNumber = "A1234BB",
            foodAllergies = mutableSetOf(FOOD_ALLERGY_PEANUTS, FOOD_ALLERGY_OTHER),
            medicalDietaryRequirements = mutableSetOf(MEDICAL_DIET_COELIAC, MEDICAL_DIET_OTHER),
            personalisedDietaryRequirements = mutableSetOf(PERSONALISED_DIET_VEGAN, PERSONALISED_DIET_OTHER),
          ),
        ),
      )

      assertThat(
        underTest.getHealthFiltersForPrison(PRISON_ID),
      ).isEqualTo(
        HealthAndMedicationFiltersResponse(
          foodAllergies = listOf(
            HealthAndMedicationFilter("Peanuts", "PEANUTS", 2),
            HealthAndMedicationFilter("Other food allergy", "OTHER", 1),
          ),
          medicalDietaryRequirements = listOf(
            HealthAndMedicationFilter("Coeliac", "COELIAC", 2),
            HealthAndMedicationFilter("Other medical diet", "OTHER", 1),
          ),
          personalisedDietaryRequirements = listOf(
            HealthAndMedicationFilter("Vegan", "VEGAN", 1),
            HealthAndMedicationFilter("Other personalised diet", "OTHER", 1),
          ),
        ),
      )
    }

    @Test
    fun `Returns an empty list when no matching prisoners are found`() {
      whenever(
        prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(
          mutableListOf(
            PRISONER_NUMBER,
          ),
        ),
      ).thenReturn(emptyList())

      assertThat(
        underTest.getHealthFiltersForPrison(PRISON_ID),
      ).isEqualTo(
        HealthAndMedicationFiltersResponse(
          foodAllergies = emptyList(),
          personalisedDietaryRequirements = emptyList(),
          medicalDietaryRequirements = emptyList(),
        ),
      )
    }

    @Test
    fun `Returns an empty list when no matching prison is found`() {
      whenever(prisonerSearchClient.getPrisonersForPrison("ABC"))
        .thenReturn(null)

      assertThat(
        underTest.getHealthFiltersForPrison("ABC"),
      ).isEqualTo(
        HealthAndMedicationFiltersResponse(
          foodAllergies = emptyList(),
          personalisedDietaryRequirements = emptyList(),
          medicalDietaryRequirements = emptyList(),
        ),
      )
    }
  }

  @Nested
  inner class SmokerStatusUpdate {
    @Test
    fun `can update the smoker vaper status`() {
      whenever(prisonApiClient.updateSmokerStatus(PRISONER_NUMBER, PrisonApiSmokerStatusUpdate(Y)))
        .thenReturn(ResponseEntity.noContent().build())

      underTest.updateSmokerStatus(PRISONER_NUMBER, UpdateSmokerStatusRequest(smokerStatus = SMOKER_CODE.id))

      verify(prisonApiClient).updateSmokerStatus(PRISONER_NUMBER, PrisonApiSmokerStatusUpdate(Y))
    }
  }

  private companion object {
    const val PRISON_ID = "STI"
    const val PRISONER_NUMBER = "A1234AA"
    const val PRISONER_FIRST_NAME = "First"
    const val PRISONER_LAST_NAME = "Last"
    const val PRISONER_LOCATION = "Recp"
    const val USER1 = "USER1"
    const val USER2 = "USER2"

    val NOW = ZonedDateTime.now()

    private fun referenceDataDomain(code: String, description: String) = ReferenceDataDomain(
      code = code,
      createdBy = USER1,
      createdAt = NOW,
      listSequence = 0,
      description = description,
    )

    private fun referenceDataCode(id: String, code: String, description: String, domain: ReferenceDataDomain) = ReferenceDataCode(
      id = id,
      code = code,
      createdBy = USER1,
      createdAt = NOW,
      description = description,
      listSequence = 1,
      domain = domain,
    )

    val SMOKER_CODE = referenceDataCode(
      id = "SMOKER_YES",
      code = "YES",
      description = "Yes, they smoke",
      domain = referenceDataDomain("SMOKER", "Smoker"),
    )

    val FOOD_ALLERGY_PEANUTS_CODE = referenceDataCode(
      id = "FOOD_ALLERGY_PEANUTS",
      code = "PEANUTS",
      description = "Peanuts",
      domain = referenceDataDomain("FOOD_ALLERGY", "Food allergy"),
    )
    val FOOD_ALLERGY_PEANUTS = FoodAllergy(prisonerNumber = PRISONER_NUMBER, allergy = FOOD_ALLERGY_PEANUTS_CODE)

    val FOOD_ALLERGY_OTHER_CODE = referenceDataCode(
      id = "FOOD_ALLERGY_OTHER",
      code = "OTHER",
      description = "Other",
      domain = referenceDataDomain("FOOD_ALLERGY", "Food allergy"),
    )
    val FOOD_ALLERGY_OTHER = FoodAllergy(prisonerNumber = PRISONER_NUMBER, allergy = FOOD_ALLERGY_OTHER_CODE)

    val MEDICAL_DIET_COELIAC_CODE = referenceDataCode(
      id = "MEDICAL_DIET_COELIAC",
      code = "COELIAC",
      description = "Coeliac",
      domain = referenceDataDomain("MEDICAL_DIET", "Medical diet"),
    )
    val MEDICAL_DIET_COELIAC = MedicalDietaryRequirement(
      prisonerNumber = PRISONER_NUMBER,
      dietaryRequirement = MEDICAL_DIET_COELIAC_CODE,
    )

    val MEDICAL_DIET_OTHER_CODE = referenceDataCode(
      id = "MEDICAL_DIET_OTHER",
      code = "OTHER",
      description = "Other",
      domain = referenceDataDomain("MEDICAL_DIET", "Medical diet"),
    )
    val MEDICAL_DIET_OTHER = MedicalDietaryRequirement(
      prisonerNumber = PRISONER_NUMBER,
      dietaryRequirement = MEDICAL_DIET_OTHER_CODE,
    )

    val PERSONALISED_DIET_VEGAN_CODE = referenceDataCode(
      id = "PERSONALISED_DIET_VEGAN",
      code = "VEGAN",
      description = "Vegan",
      domain = referenceDataDomain("PERSONALISED_DIET", "Personalised diet"),
    )
    val PERSONALISED_DIET_VEGAN = PersonalisedDietaryRequirement(
      prisonerNumber = PRISONER_NUMBER,
      dietaryRequirement = PERSONALISED_DIET_VEGAN_CODE,
    )

    val PERSONALISED_DIET_OTHER_CODE = referenceDataCode(
      id = "PERSONALISED_DIET_OTHER",
      code = "OTHER",
      description = "Other",
      domain = referenceDataDomain("PERSONALISED_DIET", "Personalised diet"),
    )
    val PERSONALISED_DIET_OTHER = PersonalisedDietaryRequirement(
      prisonerNumber = PRISONER_NUMBER,
      dietaryRequirement = PERSONALISED_DIET_OTHER_CODE,
    )

    val PRISONER_SEARCH_RESPONSE =
      PrisonerDto(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = PRISON_ID,
        firstName = PRISONER_FIRST_NAME,
        lastName = PRISONER_LAST_NAME,
        cellLocation = PRISONER_LOCATION,
      )

    val DIET_AND_ALLERGY_UPDATE_REQUEST =
      UpdateDietAndAllergyRequest(
        foodAllergies = listOf(ReferenceDataIdSelection(FOOD_ALLERGY_PEANUTS_CODE.id)),
        medicalDietaryRequirements = listOf(ReferenceDataIdSelection(MEDICAL_DIET_COELIAC_CODE.id)),
        personalisedDietaryRequirements = listOf(ReferenceDataIdSelection(PERSONALISED_DIET_VEGAN_CODE.id)),
        cateringInstructions = "Some catering instructions.",
      )

    val EMPTY_DIET_AND_ALLERGY_UPDATE_REQUEST = UpdateDietAndAllergyRequest(
      foodAllergies = emptyList(),
      medicalDietaryRequirements = emptyList(),
      personalisedDietaryRequirements = emptyList(),
      cateringInstructions = null,
    )

    val PRISONER_HEALTH = PrisonerHealth(
      prisonerNumber = PRISONER_NUMBER,
      medicalDietaryRequirements = mutableSetOf(
        MEDICAL_DIET_COELIAC,
      ),
      foodAllergies = mutableSetOf(
        FOOD_ALLERGY_PEANUTS,
      ),
      fieldMetadata = mutableMapOf(
        MEDICAL_DIET to FieldMetadata(
          PRISONER_NUMBER,
          MEDICAL_DIET,
          NOW,
          USER1,
          PRISON_ID,
        ),
        FOOD_ALLERGY to FieldMetadata(
          PRISONER_NUMBER,
          MEDICAL_DIET,
          NOW,
          USER1,
          PRISON_ID,
        ),
      ),
    )

    @JvmStatic
    fun singleHealthAndMedicationFilters(): Stream<Arguments> = Stream.of(
      Arguments.of(HealthAndMedicationRequestFilters(foodAllergies = setOf("OTHER"))),
      Arguments.of(HealthAndMedicationRequestFilters(personalisedDietaryRequirements = setOf("OTHER"))),
      Arguments.of(HealthAndMedicationRequestFilters(medicalDietaryRequirements = setOf("OTHER"))),
    )
  }
}
