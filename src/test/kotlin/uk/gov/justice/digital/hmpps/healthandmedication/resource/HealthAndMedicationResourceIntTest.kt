package uk.gov.justice.digital.hmpps.healthandmedication.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode.STRICT
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.MEDICAL_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.PERSONALISED_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergyHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.JsonObject
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementItem
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.HistoryComparison
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class HealthAndMedicationResourceIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("PUT /prisoners/{prisonerNumber}/diet-and-allergy")
  @Nested
  inner class UpdateDietAndAllergyDataTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/diet-and-allergy")
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange().expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/diet-and-allergy")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/diet-and-allergy")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG"))).header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    @Sql("classpath:jpa/repository/reset.sql")
    inner class Validation {

      @Test
      fun `bad request when field type is not as expected`() {
        expectBadRequestFrom(
          prisonerNumber = PRISONER_NUMBER,
          requestBody = """{ "foodAllergies": 123, "medicalDietaryRequirements": [] }""",
          message = "Validation failure: Couldn't read request body",
        )
      }

      @Test
      fun `bad request when prisoner not found`() {
        expectBadRequestFrom(
          prisonerNumber = "PRISONER_NOT_FOUND",
          requestBody = VALID_REQUEST_BODY,
          message = "Validation failure: Prisoner number 'PRISONER_NOT_FOUND' not found",
        )
      }

      @Nested
      inner class MedicalDietaryRequirements {
        @Test
        fun `bad request when null for medical dietary requirements`() {
          expectBadRequestFrom(
            PRISONER_NUMBER,
            requestBody = """{ "medicalDietaryRequirements": null, "foodAllergies": [] }""",
            message = "Validation failure(s): The supplied array must either be empty or contain reference data codes of the correct domain.",
          )
        }

        @Test
        fun `bad request when incorrect domain for medical dietary requirements`() {
          expectBadRequestFrom(
            PRISONER_NUMBER,
            // language=json
            requestBody = """
            { 
              "medicalDietaryRequirements": [
                { "value": "MEDICAL_DIET_COELIAC" },
                { "value": "FOOD_ALLERGY_EGG" }
              ],
              "foodAllergies": []
            }
            """.trimMargin(),
            message = "Validation failure(s): The supplied array must either be empty or contain reference data codes of the correct domain.",
          )
        }
      }

      @Nested
      inner class FoodAllergies {
        @Test
        fun `bad request when foodAllergies is null`() {
          expectBadRequestFrom(
            PRISONER_NUMBER,
            requestBody = """{ "foodAllergies": null, "medicalDietaryRequirements": [] }""",
            message = "Validation failure(s): The supplied array must either be empty or contain reference data codes of the correct domain.",
          )
        }

        @Test
        fun `bad request when incorrect domain for foodAllergies`() {
          expectBadRequestFrom(
            PRISONER_NUMBER,
            // language=json
            requestBody = """
            { 
              "foodAllergies": [
                { "value": "MEDICAL_DIET_COELIAC" },
                { "value": "FOOD_ALLERGY_EGG" }
              ],
              "medicalDietaryRequirements": []
            }
            """.trimMargin(),
            message = "Validation failure(s): The supplied array must either be empty or contain reference data codes of the correct domain.",
          )
        }
      }

      private fun expectBadRequestFrom(prisonerNumber: String, requestBody: String, message: String) {
        webTestClient.put().uri("/prisoners/$prisonerNumber/diet-and-allergy")
          .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW")))
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody().jsonPath("userMessage").isEqualTo(message)
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can create new diet and allergy data`() {
        expectSuccessfulUpdateFrom(VALID_REQUEST_BODY).expectBody().json(
          DIET_AND_ALLERGY_UPDATED_RESPONSE,
          STRICT,
        )

        expectFieldHistory(
          FOOD_ALLERGY,
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergyHistory("FOOD_ALLERGY_MILK")),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
        expectFieldHistory(
          MEDICAL_DIET,
          HistoryComparison(
            value = JsonObject(
              MEDICAL_DIET,
              MedicalDietaryRequirementHistory(
                listOf(
                  MedicalDietaryRequirementItem(
                    "MEDICAL_DIET_OTHER",
                    "Some other diet",
                  ),
                ),
              ),
            ),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
        expectFieldHistory(
          PERSONALISED_DIET,
          HistoryComparison(
            value = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory()),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:resource/healthandmedication/health.sql")
      @Sql("classpath:resource/healthandmedication/food_allergies.sql")
      @Sql("classpath:resource/healthandmedication/medical_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/personalised_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/field_metadata.sql")
      @Sql("classpath:resource/healthandmedication/field_history.sql")
      fun `can update existing diet and allergy data`() {
        expectFieldHistory(
          FOOD_ALLERGY,
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergyHistory("FOOD_ALLERGY_SOYA")),
            createdAt = THEN,
            createdBy = USER1,
          ),
        )
        expectFieldHistory(
          MEDICAL_DIET,
          HistoryComparison(
            value = JsonObject(MEDICAL_DIET, MedicalDietaryRequirementHistory("MEDICAL_DIET_LOW_CHOLESTEROL")),
            createdAt = THEN,
            createdBy = USER1,
          ),
        )
        expectFieldHistory(
          PERSONALISED_DIET,
          HistoryComparison(
            value = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory("PERSONALISED_DIET_KOSHER")),
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        expectSuccessfulUpdateFrom(VALID_REQUEST_BODY).expectBody().json(
          DIET_AND_ALLERGY_UPDATED_RESPONSE,
          STRICT,
        )

        expectFieldHistory(
          FOOD_ALLERGY,
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergyHistory("FOOD_ALLERGY_SOYA")),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergyHistory("FOOD_ALLERGY_MILK")),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
        expectFieldHistory(
          MEDICAL_DIET,
          HistoryComparison(
            value = JsonObject(MEDICAL_DIET, MedicalDietaryRequirementHistory("MEDICAL_DIET_LOW_CHOLESTEROL")),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = JsonObject(
              MEDICAL_DIET,
              MedicalDietaryRequirementHistory(
                listOf(
                  MedicalDietaryRequirementItem(
                    value = "MEDICAL_DIET_OTHER",
                    comment = "Some other diet",
                  ),
                ),
              ),
            ),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
        expectFieldHistory(
          PERSONALISED_DIET,
          HistoryComparison(
            value = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory("PERSONALISED_DIET_KOSHER")),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = JsonObject(PERSONALISED_DIET, PersonalisedDietaryRequirementHistory()),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:resource/healthandmedication/food_allergies.sql")
      @Sql("classpath:resource/healthandmedication/medical_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/personalised_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/field_metadata.sql")
      @Sql("classpath:resource/healthandmedication/field_history.sql")
      fun `can update existing diet and allergy data to empty lists`() {
        expectSuccessfulUpdateFrom(
          // language=json
          """
            { "foodAllergies": [], "medicalDietaryRequirements": [], "personalisedDietaryRequirements": [] }
          """.trimIndent(),
        ).expectBody().json(
          // language=json
          """
            {
              "foodAllergies": {
                "value": [], 
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "medicalDietaryRequirements": {
                "value": [], 
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              },
              "personalisedDietaryRequirements": {
                "value": [], 
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1"
              }
            }
          """.trimIndent(),
          STRICT,
        )
      }

      private fun expectSuccessfulUpdateFrom(requestBody: String, user: String? = USER1): WebTestClient.ResponseSpec = webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/diet-and-allergy")
        .headers(
          setAuthorisation(user, roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW")),
        )
        .header("Content-Type", "application/json")
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isOk
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    val NOW = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

    val VALID_REQUEST_BODY =
      // language=json
      """
        { 
          "foodAllergies": [{ "value": "FOOD_ALLERGY_MILK" }],
          "medicalDietaryRequirements": [{ "value": "MEDICAL_DIET_OTHER", "comment": "Some other diet" }],
          "personalisedDietaryRequirements": []
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
                "description": "Milk",
                "listSequence": 6,
                "isActive": true
              },
              "comment" : null
            }
          ],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1"
        },
        "medicalDietaryRequirements": {
          "value": [
            {
              "value": {
                "id": "MEDICAL_DIET_OTHER",
                "description": "Other",
                "listSequence": 8,
                "isActive": true
              },
              "comment" : "Some other diet" 
            } 
          ],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1"
        },
        "personalisedDietaryRequirements": {
          "value": [],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1"
        } 
      }
      """.trimIndent()
  }
}
