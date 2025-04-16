package uk.gov.justice.digital.hmpps.healthandmedication.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.integration.TestBase
import java.time.Clock
import java.time.ZonedDateTime

/*
  From https://dsdmoj.atlassian.net/wiki/spaces/SARS/pages/4780589084/Integration+Guide#Testing-Checklist
   - Does the endpoint respond with a 200 status code when hitting the health endpoint?
   - Does the endpoint respond with a 401 status code when the SAR endpoint is hit without a token? YES
   - Does the endpoint respond with a 403 status code when the SAR endpoint is hit with a token that does not have the ROLE_SAR_DATA_ACCESS role? YES
   - Does the endpoint respond with a 209 status code and empty response body when the SAR endpoint is hit with a valid token but
     with a subject identifier that this service does not use (eg. a CRN is provided but the service only contains data relating
     to PRNs)? YES
   - Does the endpoint respond with a 204 status code and empty response body when the SAR endpoint is hit with a valid token
     and a placeholder subject identifier that has no data? YES
   - Does the endpoint respond with a 200 status code when the SAR endpoint is hit with a valid token and valid subject identifier?
   - Does the endpoint respond with a response body in JSON format with a JSON object in the ‘content’ block?
*/
class SubjectAccessRequestResourceIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("GET subject-access-request?prn=\${PRISONER_NUMBER")
  @Nested
  inner class SecurityTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/subject-access-request?prn=${PRISONER_NUMBER}")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/subject-access-request?prn=${PRISONER_NUMBER}")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/subject-access-request?prn=${PRISONER_NUMBER}")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }
  }

  @Nested
  @Sql("classpath:jpa/repository/reset.sql")
  inner class Validation {

    @Test
    fun `209 status code and empty response body when the SAR endpoint is hit with a valid token but with a subject identifier that this service does not use`() {
      webTestClient.get().uri("/subject-access-request?crn=${PRISONER_NUMBER}")
        .headers(setAuthorisation(roles = listOf("SAR_DATA_ACCESS")))
        .exchange()
        .expectStatus().isEqualTo(209)
    }

    @Test
    fun `Does the endpoint respond with a 204 status code and empty response body when the SAR endpoint is hit with a valid token and a placeholder subject identifier that has no data`() {
      webTestClient.get().uri("/subject-access-request?prn=${PRISONER_NUMBER}")
        .headers(setAuthorisation(roles = listOf("SAR_DATA_ACCESS")))
        .exchange()
        .expectStatus().isEqualTo(204)
        .expectBody().isEmpty()
    }

    @Test
    fun `Does the endpoint respond with a 200 status code when the SAR endpoint is hit with a valid token and valid subject identifier`() {
      webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/diet-and-allergy")
        .headers(
          setAuthorisation(USER1, roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW")),
        )
        .header("Content-Type", "application/json")
        .bodyValue(VALID_DIET_AND_FOOD_ALLERGY_REQUEST)
        .exchange()
        .expectStatus().isOk
        .expectBody().json(DIET_AND_ALLERGY_UPDATED_RESPONSE, JsonCompareMode.STRICT)

      webTestClient.get().uri("/subject-access-request?prn=${PRISONER_NUMBER}")
        .headers(setAuthorisation(roles = listOf("SAR_DATA_ACCESS")))
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isOk()
        .expectBody().json(SAR_RESPONSE_WITHOUT_FIELD_HISTORY_ID, JsonCompareMode.LENIENT)
        .jsonPath("$.content.[0].fieldHistoryId").isEqualTo(EXPECTED_FIRST_FIELD_HISTORY_ID)

      // Relies on resetting field_history_field_history_id_seq in reset.sql.

      // Note that when creating data using /diet-and-allergy the FieldHistory record for each of the 4 fields is created
      // in a random order. This means that when we create FieldHistory record for FoodAllergies the fieldHistoryId is
      // not guaranteed to be the same for each invocation. Consequently, we use lenient checking and omit the
      // fieldHistoryId from SAR_RESPONSE_WITHOUT_FIELD_HISTORY_ID. Because the SAR contract is to show the most recent
      // information first, we can instead check that the first item in the SAR Response data has a fieldHistoryId
      // of 4 - see EXPECTED_FIRST_FIELD_HISTORY_ID.
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"

    const val EXPECTED_FIRST_FIELD_HISTORY_ID = 4

    val NOW: ZonedDateTime = ZonedDateTime.now(TestBase.clock)

    val VALID_DIET_AND_FOOD_ALLERGY_REQUEST =
      // language=json
      """
        { 
          "foodAllergies": [{ "value": "FOOD_ALLERGY_MILK" }],
          "medicalDietaryRequirements": [{ "value": "MEDICAL_DIET_OTHER", "comment": "Some other diet" }],
          "personalisedDietaryRequirements": [],
          "cateringInstructions": "Some catering instructions."
        }
      """.trimIndent()

    val DIET_AND_ALLERGY_UPDATED_RESPONSE =
      // language=json
      """
      {
        "foodAllergies": {
          "value": [
            {
              "value": {
                "id": "FOOD_ALLERGY_MILK",
                "code": "MILK",
                "description": "Milk"
              },
              "comment" : null
            }
          ],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1",
          "lastModifiedPrisonId": "MDI"
        },
        "medicalDietaryRequirements": {
          "value": [
            {
              "value": {
                "id": "MEDICAL_DIET_OTHER",
                "code": "OTHER",
                "description": "Other"
              },
              "comment" : "Some other diet" 
            } 
          ],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1",
          "lastModifiedPrisonId": "MDI"
        },
        "personalisedDietaryRequirements": {
          "value": [],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1",
          "lastModifiedPrisonId": "MDI"
        },
        "cateringInstructions": {
          "value": "Some catering instructions.",
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1",
          "lastModifiedPrisonId": "MDI"
        }
      }
      """.trimIndent()

    val SAR_RESPONSE_WITHOUT_FIELD_HISTORY_ID =
      // language=json
      """
          {
            "content": [   
                          {
                          "prisonerNumber":"A1234AA",
                          "fieldHistoryType":"Catering Instructions",
                          "fieldHistoryValue":"Some catering instructions.",
                          "createdAt":"2024-06-14T09:10:11+0100",
                          "createdBy":"USER1",
                          "mergedAt":null,
                          "mergedFrom":null,
                          "prisonId":"MDI"
                          },
                          {
                            "prisonerNumber":"A1234AA",
                            "fieldHistoryType":"Personalised Dietary Requirements",
                            "fieldHistoryValue":[],
                            "createdAt":"2024-06-14T09:10:11+0100",
                            "createdBy":"USER1",
                            "mergedAt":null,
                            "mergedFrom":null,
                            "prisonId":"MDI"
                          },
                          {
                            "prisonerNumber":"A1234AA",
                            "fieldHistoryType":"Medical Dietary Requirements",
                            "fieldHistoryValue":[{"value":"Other","comment":"Some other diet"}],
                            "createdAt":"2024-06-14T09:10:11+0100",
                            "createdBy":"USER1",
                            "mergedAt":null,
                            "mergedFrom":null,
                            "prisonId":"MDI"
                          },
                          {
                            "prisonerNumber":"A1234AA",
                            "fieldHistoryType":"Food Allergies",
                            "fieldHistoryValue":[{"value":"Milk","comment":null}],
                            "createdAt":"2024-06-14T09:10:11+0100",
                            "createdBy":"USER1",
                            "mergedAt":null,
                            "mergedFrom":null,
                            "prisonId":"MDI"
                          }
                      ]
          }
      """.trimIndent()
  }
}
