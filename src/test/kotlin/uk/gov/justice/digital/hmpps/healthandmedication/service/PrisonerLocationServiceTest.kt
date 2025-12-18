package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.AssignedLivingUnitDto
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.OffenderDto
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.response.PrisonerDto
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerLocation
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.PrisonerHealthRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.PrisonerLocationRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class PrisonerLocationServiceTest {

  @Mock
  lateinit var prisonerLocationRepository: PrisonerLocationRepository

  @Mock
  lateinit var prisonerHealthRepository: PrisonerHealthRepository

  @Mock
  lateinit var prisonApiClient: PrisonApiClient

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @InjectMocks
  lateinit var underTest: PrisonerLocationService

  @BeforeEach
  fun beforeEach() {
    whenever(prisonApiClient.getOffender(PRISONER_NUMBER)).thenReturn(
      OffenderDto(
        PRISONER_NUMBER,
        AssignedLivingUnitDto(
          agencyId = "STI",
          agencyName = "STYAL (HMP & YOI)",
          description = "E-2-014",
          locationId = 123,
        ),
      ),
    )
    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
      PrisonerDto(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        firstName = "John",
        lastName = "Smith",
        cellLocation = "E-2-014",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      ),
    )
    whenever(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).thenReturn(
      listOf(PrisonerHealth(PRISONER_NUMBER)),
    )
  }

  @DisplayName("GetLatestLocationData - Prisoner Search Only")
  @Nested
  inner class GetLatestLocationDataPrisonerSearchOnly {
    @Test
    fun `should fetch latest location data`() {
      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        topLocationLevel = "E",
        location = "E-2-014",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = true)

      assertThat(location).isEqualTo(expected)
    }

    @Test
    fun `no data found in upstream APIs`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(null)
      whenever(prisonApiClient.getOffender(PRISONER_NUMBER)).thenReturn(null)

      assertThat(underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = true)).isNull()
    }

    @Test
    fun `no data from Prisoner Search API`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(null)

      assertThat(underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = true)).isNull()
    }

    @Test
    fun `empty location data`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
        PrisonerDto(
          prisonerNumber = PRISONER_NUMBER,
          prisonId = "STI",
          firstName = "John",
          lastName = "Smith",
          cellLocation = null,
          lastAdmissionDate = LocalDate.parse("2025-11-01"),
        ),
      )

      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = true)

      assertThat(location).isEqualTo(expected)
    }

    @Test
    fun `no location data (prisoner not currently in prison)`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(
        PrisonerDto(
          prisonerNumber = PRISONER_NUMBER,
          prisonId = null,
          firstName = "John",
          lastName = "Smith",
          cellLocation = null,
          lastAdmissionDate = LocalDate.parse("2025-11-01"),
        ),
      )

      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = true)

      assertThat(location).isEqualTo(expected)
    }
  }

  @DisplayName("GetLatestLocationData - Prisoner Search & Prison API")
  @Nested
  inner class GetLatestLocationDataPrisoner {
    @Test
    fun `should fetch latest location data`() {
      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        topLocationLevel = "E",
        location = "E-2-014",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = false)

      assertThat(location).isEqualTo(expected)
    }

    @Test
    fun `no data from Prisoner Search API`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(null)

      assertThat(underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = false)).isNull()
    }

    @Test
    fun `no data from Prison API`() {
      whenever(prisonApiClient.getOffender(PRISONER_NUMBER)).thenReturn(null)
      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      assertThat(underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = false)).isEqualTo(expected)
    }

    @Test
    fun `empty location data`() {
      whenever(prisonApiClient.getOffender(PRISONER_NUMBER)).thenReturn(
        OffenderDto(
          PRISONER_NUMBER,
          AssignedLivingUnitDto(
            agencyId = "STI",
            agencyName = "STYAL (HMP & YOI)",
            description = null,
            locationId = 123,
          ),
        ),
      )

      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = false)

      assertThat(location).isEqualTo(expected)
    }

    @Test
    fun `no location data (prisoner not currently in prison)`() {
      whenever(prisonApiClient.getOffender(PRISONER_NUMBER)).thenReturn(
        OffenderDto(
          PRISONER_NUMBER,
          null,
        ),
      )

      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER, usePrisonerSearchOnly = false)

      assertThat(location).isEqualTo(expected)
    }
  }

  @Nested
  inner class UpdateLocationDataToLatest {
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should be able to fetch and persist latest location data`(usePrisonerSearchOnly: Boolean) {
      val savedLocation = argumentCaptor<PrisonerLocation>()
      whenever(prisonerLocationRepository.save(savedLocation.capture())).thenAnswer { savedLocation.firstValue }
      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        topLocationLevel = "E",
        location = "E-2-014",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      underTest.updateLocationDataToLatest(PRISONER_NUMBER, usePrisonerSearchOnly)

      assertThat(savedLocation.firstValue).isEqualTo(expected)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `does not persist latest location data if there is no existing health data for the prisoner`(usePrisonerSearchOnly: Boolean) {
      whenever(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).thenReturn(
        listOf(),
      )

      underTest.updateLocationDataToLatest(PRISONER_NUMBER, usePrisonerSearchOnly)

      verify(prisonerLocationRepository, never()).save(any())
    }
  }

  @Nested
  inner class MigrateLocationData {
    @Test
    fun `should migrate location data`() {
      val savedValue = argumentCaptor<PrisonerLocation>()
      whenever(prisonerHealthRepository.findAllPrisonersWithoutLocationData()).thenReturn(
        listOf(
          PrisonerHealth(PRISONER_NUMBER),
        ),
      )
      whenever(prisonerLocationRepository.save(savedValue.capture())).thenAnswer { savedValue.firstValue }
      val expectedSavedLocation = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
        topLocationLevel = "E",
        location = "E-2-014",
      )
      underTest.migrateLocationData()

      verify(prisonerLocationRepository, times(1)).save(any())
      assertThat(savedValue.firstValue).isEqualTo(expectedSavedLocation)
    }
  }

  companion object {
    private const val PRISONER_NUMBER = "A1234AA"
  }
}
