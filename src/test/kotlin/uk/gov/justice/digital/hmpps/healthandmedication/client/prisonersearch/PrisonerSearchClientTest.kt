package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.healthandmedication.config.DownstreamServiceException
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER_NOT_FOUND
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER_THROW_EXCEPTION
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISON_ID
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PrisonerSearchServer

class PrisonerSearchClientTest {
  private lateinit var client: PrisonerSearchClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = PrisonerSearchClient(webClient)
  }

  @Test
  fun `getPrisoner - success`() {
    server.stubGetPrisoner()

    val result = client.getPrisoner(PRISONER_NUMBER)

    assertThat(result!!).isEqualTo(PrisonerDto(prisonerNumber = PRISONER_NUMBER, prisonId = PRISON_ID))
  }

  @Test
  fun `getPrisoner - prisoner not found`() {
    val result = client.getPrisoner(PRISONER_NUMBER_NOT_FOUND)

    assertThat(result).isNull()
  }

  @Test
  fun `getPrisoner - downstream service exception`() {
    server.stubGetPrisonerException()

    assertThatThrownBy { client.getPrisoner(PRISONER_NUMBER_THROW_EXCEPTION) }
      .isInstanceOf(DownstreamServiceException::class.java)
      .hasMessage("Get prisoner request failed")
      .hasCauseInstanceOf(WebClientResponseException::class.java)
      .hasRootCauseMessage("500 Internal Server Error from GET http://localhost:8112/prisoner/${PRISONER_NUMBER_THROW_EXCEPTION}")
  }

  companion object {
    @JvmField
    internal val server = PrisonerSearchServer()

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
