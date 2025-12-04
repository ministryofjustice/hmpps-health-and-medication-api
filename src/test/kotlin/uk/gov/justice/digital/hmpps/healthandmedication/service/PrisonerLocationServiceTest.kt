package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.HousingLevelDto
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.PrisonerHousingLocationDto
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
    whenever(prisonApiClient.getHousingLocation(PRISONER_NUMBER)).thenReturn(
      PrisonerHousingLocationDto(
        listOf(
          HousingLevelDto(3, "014", "Cell 14"),
          HousingLevelDto(1, "E", "E Wing"),
          HousingLevelDto(2, "2", "Block 2"),
        ),
      ),
    )
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
    whenever(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).thenReturn(
      listOf(PrisonerHealth(PRISONER_NUMBER)),
    )
  }

  @Nested
  inner class GetLatestLocationData {
    @Test
    fun `should fetch latest location data`() {
      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        topLevelCode = "E",
        topLevelDescription = "E Wing",
        location = "E-2-014",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER)

      assertThat(location).isEqualTo(expected)
    }

    @Test
    fun `no data found in upstream APIs`() {
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(null)
      whenever(prisonApiClient.getHousingLocation(PRISONER_NUMBER)).thenReturn(null)

      assertThat(underTest.getLatestLocationData(PRISONER_NUMBER)).isNull()
    }

    @Test
    fun `no data from Prison API`() {
      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )
      whenever(prisonApiClient.getHousingLocation(PRISONER_NUMBER)).thenReturn(null)

      assertThat(underTest.getLatestLocationData(PRISONER_NUMBER)).isEqualTo(expected)
    }

    @Test
    fun `no data from Prisoner Search API`() {
      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        topLevelCode = "E",
        topLevelDescription = "E Wing",
        location = "E-2-014",
      )
      whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER)).thenReturn(null)

      assertThat(underTest.getLatestLocationData(PRISONER_NUMBER)).isEqualTo(expected)
    }

    @Test
    fun `empty location data`() {
      whenever(prisonApiClient.getHousingLocation(PRISONER_NUMBER)).thenReturn(PrisonerHousingLocationDto(listOf()))

      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER)

      assertThat(location).isEqualTo(expected)
    }

    @Test
    fun `no location data (prisoner not currently in prison)`() {
      whenever(prisonApiClient.getHousingLocation(PRISONER_NUMBER)).thenReturn(PrisonerHousingLocationDto(null))

      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      val location = underTest.getLatestLocationData(PRISONER_NUMBER)

      assertThat(location).isEqualTo(expected)
    }
  }

  @Nested
  inner class UpdateLocationDataToLatest {
    @Test
    fun `should be able to fetch and persist latest location data`() {
      val savedLocation = argumentCaptor<PrisonerLocation>()
      whenever(prisonerLocationRepository.save(savedLocation.capture())).thenAnswer { savedLocation.firstValue }
      val expected = PrisonerLocation(
        prisonerNumber = PRISONER_NUMBER,
        prisonId = "STI",
        topLevelCode = "E",
        topLevelDescription = "E Wing",
        location = "E-2-014",
        lastAdmissionDate = LocalDate.parse("2025-11-01"),
      )

      underTest.updateLocationDataToLatest(PRISONER_NUMBER)

      assertThat(savedLocation.firstValue).isEqualTo(expected)
    }

    @Test
    fun `does not persist latest location data if there is no existing health data for the prisoner`() {
      whenever(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).thenReturn(
        listOf(),
      )

      underTest.updateLocationDataToLatest(PRISONER_NUMBER)

      verify(prisonerLocationRepository, never()).save(any())
    }
  }

  companion object {
    private const val PRISONER_NUMBER = "A1234AA"
  }
}
