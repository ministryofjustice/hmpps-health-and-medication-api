package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatus.Y
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatusUpdate
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.HousingLevelDto
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.PrisonerHousingLocationDto
import uk.gov.justice.digital.hmpps.healthandmedication.config.DownstreamServiceException
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER_EMPTY_RESPONSE
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER_NOT_FOUND
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER_THROW_EXCEPTION
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PrisonApiMockServer

class PrisonApiClientTest {
  private lateinit var client: PrisonApiClient

  @Nested
  inner class UpdateSmokerStatus {
    @BeforeEach
    fun resetMocks() {
      server.resetRequests()
      server.stubUpdateSmokerStatus()
      val webClient = WebClient.create("http://localhost:${server.port()}")
      client = PrisonApiClient(webClient)
    }

    @Test
    fun success() {
      val result = client.updateSmokerStatus(PRISONER_NUMBER, PrisonApiSmokerStatusUpdate(smokerStatus = Y))

      assertThat(result!!.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }

    @Test
    fun `prisoner not found`() {
      assertThatThrownBy {
        client.updateSmokerStatus(
          PRISONER_NUMBER_NOT_FOUND,
          PrisonApiSmokerStatusUpdate(smokerStatus = Y),
        )
      }
        .isInstanceOf(DownstreamServiceException::class.java)
        .hasMessage("Update smoker status request failed")
        .hasCauseInstanceOf(WebClientResponseException::class.java)
        .hasRootCauseMessage("404 Not Found from PUT http://localhost:8091/api/offenders/${PRISONER_NUMBER_NOT_FOUND}/smoker")
    }

    @Test
    fun `downstream service exception`() {
      assertThatThrownBy {
        client.updateSmokerStatus(
          PRISONER_NUMBER_THROW_EXCEPTION,
          PrisonApiSmokerStatusUpdate(smokerStatus = Y),
        )
      }
        .isInstanceOf(DownstreamServiceException::class.java)
        .hasMessage("Update smoker status request failed")
        .hasCauseInstanceOf(WebClientResponseException::class.java)
        .hasRootCauseMessage("500 Internal Server Error from PUT http://localhost:8091/api/offenders/${PRISONER_NUMBER_THROW_EXCEPTION}/smoker")
    }
  }

  @Nested
  inner class GetHousingLocation {
    @BeforeEach
    fun resetMocks() {
      server.resetRequests()
      server.stubGetHousingLocation()
      val webClient = WebClient.create("http://localhost:${server.port()}")
      client = PrisonApiClient(webClient)
    }

    @Test
    fun success() {
      val expected = PrisonerHousingLocationDto(
        listOf(
          HousingLevelDto(1, "E", "Wing E"),
          HousingLevelDto(2, "9", "Block 9"),
          HousingLevelDto(3, "011", "Cell 011"),
        ),
      )

      val result = client.getHousingLocation(PRISONER_NUMBER)

      assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `empty housing location`() {
      val expected = PrisonerHousingLocationDto(null)

      val result = client.getHousingLocation(PRISONER_NUMBER_EMPTY_RESPONSE)

      assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `prisoner not found`() {
      assertNull(client.getHousingLocation(PRISONER_NUMBER_NOT_FOUND))
    }

    @Test
    fun `downstream service exception`() {
      assertThatThrownBy {
        client.getHousingLocation(PRISONER_NUMBER_THROW_EXCEPTION)
      }
        .isInstanceOf(DownstreamServiceException::class.java)
        .hasMessage("Get housing location request failed")
        .hasCauseInstanceOf(WebClientResponseException::class.java)
        .hasRootCauseMessage("500 Internal Server Error from GET http://localhost:8091/api/offenders/${PRISONER_NUMBER_THROW_EXCEPTION}/housing-location")
    }
  }

  companion object {
    @JvmField
    internal val server = PrisonApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      server.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      server.stop()
    }
  }
}
