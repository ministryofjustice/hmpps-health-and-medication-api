package uk.gov.justice.digital.hmpps.healthandmedication.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergies
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.JsonObject
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.HistoryComparison
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class PrisonerPrisonerHealthControllerIntTest : IntegrationTestBase() {

  @TestConfiguration
  class FixedClockConfig {
    @Primary
    @Bean
    fun fixedClock(): Clock = clock
  }

  @DisplayName("PATCH /prisoners/{prisonerNumber}")
  @Nested
  inner class SetPrisonerHealthTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}").header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange().expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}").headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json").bodyValue(VALID_REQUEST_BODY).exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG"))).header("Content-Type", "application/json")
          .bodyValue(VALID_REQUEST_BODY).exchange().expectStatus().isForbidden
      }
    }

    @Nested
    @Sql("classpath:jpa/repository/reset.sql")
    inner class Validation {

      @Test
      fun `bad request when field type is not as expected`() {
        expectBadRequestFrom(
          prisonerNumber = PRISONER_NUMBER,
          requestBody = """{ "foodAllergies": 123 }""",
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
            requestBody = """{ "medicalDietaryRequirements": null }""",
            message = "Validation failure(s): The value must be a a list of domain codes of the correct domain, an empty list, or Undefined.",
          )
        }

        @Test
        fun `bad request when incorrect domain for medical dietary requirements`() {
          expectBadRequestFrom(
            PRISONER_NUMBER,
            requestBody = """{ "medicalDietaryRequirements": ["MEDICAL_DIET_FREE_FROM","FOOD_ALLERGY_EGG"] }""".trimMargin(),
            message = "Validation failure(s): The value must be a a list of domain codes of the correct domain, an empty list, or Undefined.",
          )
        }
      }

      @Nested
      inner class FoodAllergies {
        @Test
        fun `bad request when null for medical dietary requirements`() {
          expectBadRequestFrom(
            PRISONER_NUMBER,
            requestBody = """{ "foodAllergies": null }""",
            message = "Validation failure(s): The value must be a a list of domain codes of the correct domain, an empty list, or Undefined.",
          )
        }

        @Test
        fun `bad request when incorrect domain for medical dietary requirements`() {
          expectBadRequestFrom(
            PRISONER_NUMBER,
            requestBody = """{ "foodAllergies": ["MEDICAL_DIET_FREE_FROM","FOOD_ALLERGY_EGG"] }""".trimMargin(),
            message = "Validation failure(s): The value must be a a list of domain codes of the correct domain, an empty list, or Undefined.",
          )
        }
      }

      private fun expectBadRequestFrom(prisonerNumber: String, requestBody: String, message: String) {
        webTestClient.patch().uri("/prisoners/$prisonerNumber")
          .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW")))
          .header("Content-Type", "application/json").bodyValue(requestBody).exchange()
          .expectStatus().isBadRequest.expectBody().jsonPath("userMessage").isEqualTo(message)
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      fun `can create new health information`() {
        expectSuccessfulUpdateFrom(VALID_REQUEST_BODY).expectBody().json(
          MILK_ALLERGY_ALL_UPDATED_RESPONSE,
          true,
        )

        expectFieldHistory(
          FOOD_ALLERGY,
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergies(listOf("FOOD_ALLERGY_MILK"))),
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
      @Sql("classpath:resource/healthandmedication/field_metadata.sql")
      @Sql("classpath:resource/healthandmedication/field_history.sql")
      fun `can update existing health information`() {
        expectFieldHistory(
          FOOD_ALLERGY,
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergies(listOf("FOOD_ALLERGY_SOYA"))),
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        expectSuccessfulUpdateFrom(VALID_REQUEST_BODY).expectBody().json(
          MILK_ALLERGY_MEDICAL_NOT_UPDATED_RESPONSE,
          true,
        )

        expectFieldHistory(
          FOOD_ALLERGY,
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergies(listOf("FOOD_ALLERGY_SOYA"))),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergies(listOf("FOOD_ALLERGY_MILK"))),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:resource/healthandmedication/food_allergies.sql")
      @Sql("classpath:resource/healthandmedication/medical_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/field_metadata.sql")
      @Sql("classpath:resource/healthandmedication/field_history.sql")
      fun `can update existing health information to empty list`() {
        expectFieldHistory(
          FOOD_ALLERGY,
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergies(listOf("FOOD_ALLERGY_SOYA"))),
            createdAt = THEN,
            createdBy = USER1,
          ),
        )

        expectSuccessfulUpdateFrom(
          // language=json
          """
            { "foodAllergies": [], "medicalDietaryRequirements": [] }
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
              }
            }
          """.trimIndent(),
          true,
        )

        expectFieldHistory(
          FOOD_ALLERGY,
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergies(listOf("FOOD_ALLERGY_SOYA"))),
            createdAt = THEN,
            createdBy = USER1,
          ),
          HistoryComparison(
            value = JsonObject(FOOD_ALLERGY, FoodAllergies(emptyList<String>())),
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      private fun expectSuccessfulUpdateFrom(requestBody: String, user: String? = USER1) =
        webTestClient.patch().uri("/prisoners/${PRISONER_NUMBER}")
          .headers(setAuthorisation(user, roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW")))
          .header("Content-Type", "application/json").bodyValue(requestBody).exchange().expectStatus().isOk
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
          "foodAllergies": ["FOOD_ALLERGY_MILK"]
        }
      """.trimIndent()

    val MILK_ALLERGY_ALL_UPDATED_RESPONSE =
      // language=json
      """
      {
        "foodAllergies": {
          "value": [
            {
              "id": "FOOD_ALLERGY_MILK",
              "description": "Milk",
              "listSequence": 6,
              "isActive": true
            }
          ],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1"
        },
        "medicalDietaryRequirements": {
          "value": [],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1"
        }
      }
      """.trimIndent()

    val MILK_ALLERGY_MEDICAL_NOT_UPDATED_RESPONSE =
      // language=json
      """
      {
        "foodAllergies": {
          "value": [
            {
              "id": "FOOD_ALLERGY_MILK",
              "description": "Milk",
              "listSequence": 6,
              "isActive": true
            }
          ],
          "lastModifiedAt": "2024-06-14T09:10:11+0100",
          "lastModifiedBy": "USER1"
        },
        "medicalDietaryRequirements": {
          "value": [
            {
              "id": "MEDICAL_DIET_LOW_CHOLESTEROL",
              "description": "Low cholesterol",
              "listSequence": 6,
              "isActive": true
            }
          ],
          "lastModifiedAt": "2024-01-02T09:10:11+0000",
          "lastModifiedBy": "USER1"
        }
      }
      """.trimIndent()
  }
}
