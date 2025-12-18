package uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.AssignedLivingUnitDto
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.OffenderDto

internal const val PRISON_API_NOT_FOUND_RESPONSE = """
  {
    "status": 404,
    "errorCode": "12345",
    "userMessage": "Prisoner not found",
    "developerMessage": "Prisoner not found"
  }
"""

class PrisonApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8091
  }

  private val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/health/ping").willReturn(
        aResponse().withHeader("Content-Type", "application/json")
          .withBody("""{"status":"${if (status == 200) "UP" else "DOWN"}"}""").withStatus(status),
      ),
    )
  }

  fun stubUpdateSmokerStatus() {
    val endpoint = "smoker"
    stubOffenderPutEndpoint(endpoint, HttpStatus.NO_CONTENT, PRISONER_NUMBER)
    stubOffenderPutEndpoint(endpoint, HttpStatus.INTERNAL_SERVER_ERROR, PRISONER_NUMBER_THROW_EXCEPTION)
    stubOffenderPutEndpoint(
      endpoint,
      HttpStatus.NOT_FOUND,
      PRISONER_NUMBER_NOT_FOUND,
      PRISON_API_NOT_FOUND_RESPONSE.trimIndent(),
    )
  }

  fun stubGetOffender() {
    stubOffenderGetEndpoint(
      null,
      HttpStatus.OK,
      PRISONER_NUMBER,
      mapper.writeValueAsString(
        OffenderDto(
          PRISONER_NUMBER,
          AssignedLivingUnitDto(
            PRISON_ID,
            "MOORLAND CLOSED (HMP & YOI)",
            "E-9-011",
            123,
          ),
        ),
      ),
    )
    stubOffenderGetEndpoint(null, HttpStatus.INTERNAL_SERVER_ERROR, PRISONER_NUMBER_THROW_EXCEPTION)
    stubOffenderGetEndpoint(
      null,
      HttpStatus.NOT_FOUND,
      PRISONER_NUMBER_NOT_FOUND,
      PRISON_API_NOT_FOUND_RESPONSE.trimIndent(),
    )
  }

  private fun stubOffenderPutEndpoint(endpoint: String, status: HttpStatus, prisonerNumber: String, body: String? = null) {
    stubFor(
      put(urlPathMatching("/api/offenders/$prisonerNumber/$endpoint")).willReturn(
        aResponse().withHeader("Content-Type", "application/json")
          .withStatus(status.value())
          .withBody(body),
      ),
    )
  }

  private fun stubOffenderGetEndpoint(endpoint: String?, status: HttpStatus, prisonerNumber: String, body: String? = null) {
    val suffix = endpoint?.let { "/$it" } ?: ""
    stubFor(
      get(urlPathMatching("/api/offenders/${prisonerNumber}$suffix")).willReturn(
        aResponse().withHeader("Content-Type", "application/json")
          .withStatus(status.value())
          .withBody(body),
      ),
    )
  }
}

class PrisonApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val prisonApi = PrisonApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext): Unit = prisonApi.start()
  override fun beforeEach(context: ExtensionContext) {
    prisonApi.resetAll()
    prisonApi.stubUpdateSmokerStatus()
    prisonApi.stubGetOffender()
  }

  override fun afterAll(context: ExtensionContext): Unit = prisonApi.stop()
}
