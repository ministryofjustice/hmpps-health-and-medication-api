package uk.gov.justice.digital.hmpps.healthandmedication.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PrisonerSearchExtension
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldMetadata
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.expectFieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.expectNoFieldHistoryFor
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class, PrisonerSearchExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun <T> expectFieldHistory(field: HealthAndMedicationField, vararg comparison: HistoryComparison<T>) =
    expectFieldHistory(field, fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER), *comparison)

  protected fun expectNoFieldHistoryFor(vararg field: HealthAndMedicationField) {
    val history = fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER)
    field.forEach { expectNoFieldHistoryFor(it, history) }
  }

  protected fun expectFieldMetadata(prisonerNumber: String, vararg comparison: FieldMetadata) {
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(prisonerNumber)).containsExactlyInAnyOrder(*comparison)
  }

  protected fun expectFieldMetadata(vararg comparison: FieldMetadata) =
    expectFieldMetadata(PRISONER_NUMBER, *comparison)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }
}
