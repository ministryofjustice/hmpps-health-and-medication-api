package uk.gov.justice.digital.hmpps.healthandmedication.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase

class PrisonsHealthControllerIntTest : IntegrationTestBase() {

  @DisplayName("GET /prisons/{prisonId}")
  @Nested
  inner class PrisonsHealthTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.post().uri("/prisons/$PRISON_ID").header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange().expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.post().uri("/prisons/$PRISON_ID").headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json").bodyValue(VALID_REQUEST_BODY).exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.post().uri("/prisons/$PRISON_ID")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG"))).header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange().expectStatus().isForbidden
      }
    }
  }

  private companion object {
    const val PRISON_ID = "LEI"
    val VALID_REQUEST_BODY =
      // language=json
      """
      { "page": 1, "size": 10 }  
      """.trimIndent()
  }
}
