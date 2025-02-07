package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatus.Y
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatusUpdate
import uk.gov.justice.digital.hmpps.healthandmedication.config.DownstreamServiceException
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER_NOT_FOUND
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER_THROW_EXCEPTION
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PrisonApiMockServer

class PrisonApiClientTest {
  private lateinit var client: PrisonApiClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    server.stubUpdateSmokerStatus()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = PrisonApiClient(webClient)
  }

  @Test
  fun `updateSmokerStatus - success`() {
    val result = client.updateSmokerStatus(PRISONER_NUMBER, PrisonApiSmokerStatusUpdate(smokerStatus = Y))

    assertThat(result!!.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
  }

  @Test
  fun `updateSmokerStatus - prisoner not found`() {
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
  fun `updateSmokerStatus - downstream service exception`() {
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
