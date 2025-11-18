package uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.response.PrisonerDto
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.response.PrisonerSearchResultDto

class PrisonerSearchServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8092
  }

  private val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) """{"status":"UP"}""" else """{"status":"DOWN"}""")
          .withStatus(status),
      ),
    )
  }

  fun stubGetPrisoner(prisonNumber: String = PRISONER_NUMBER): StubMapping = stubFor(
    get("/prisoner/$prisonNumber")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(PrisonerDto(prisonerNumber = prisonNumber, prisonId = PRISON_ID)),
          )
          .withStatus(200),
      ),
  )

  fun stubGetPrisonerException(prisonNumber: String = PRISONER_NUMBER_THROW_EXCEPTION): StubMapping = stubFor(get("/prisoner/$prisonNumber").willReturn(aResponse().withStatus(500)))

  fun stubGetPrisonersInPrison(prisonId: String = PRISON_ID): StubMapping = stubFor(
    get(urlPathMatching("/prison/$prisonId/prisoners"))
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(
              PrisonerSearchResultDto(
                listOf(
                  PrisonerDto(prisonerNumber = "A1234AA", prisonId = prisonId),
                  PrisonerDto(prisonerNumber = "B1234CC", prisonId = prisonId),
                ),
              ),
            ),
          )
          .withStatus(200),
      ),
  )
}

class PrisonerSearchExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val prisonerSearch = PrisonerSearchServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    prisonerSearch.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    prisonerSearch.resetRequests()
    prisonerSearch.stubGetPrisoner()
    prisonerSearch.stubGetPrisonerException()
    prisonerSearch.stubGetPrisonersInPrison()
  }

  override fun afterAll(context: ExtensionContext) {
    prisonerSearch.stop()
  }
}
