package uk.gov.justice.digital.hmpps.healthandmedication.resource

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode.STRICT
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.CATERING_INSTRUCTIONS
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.MEDICAL_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.PERSONALISED_DIET
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergyHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.JsonObject
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirementItem
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PersonalisedDietaryRequirementHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.HistoryComparison
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.RepopulateDb
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
          .bodyValue(VALID_DIET_AND_FOOD_ALLERGY_REQUEST)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/diet-and-allergy")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_DIET_AND_FOOD_ALLERGY_REQUEST)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/diet-and-allergy")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_DIET_AND_FOOD_ALLERGY_REQUEST)
          .exchange()
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
          requestBody = VALID_DIET_AND_FOOD_ALLERGY_REQUEST,
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
        expectSuccessfulUpdateFrom(VALID_DIET_AND_FOOD_ALLERGY_REQUEST).expectBody().json(
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
        expectFieldHistory(
          CATERING_INSTRUCTIONS,
          HistoryComparison(
            value = "Some catering instructions.",
            createdAt = NOW,
            createdBy = USER1,
          ),
        )
      }

      @Test
      @RepopulateDb
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

        expectSuccessfulUpdateFrom(VALID_DIET_AND_FOOD_ALLERGY_REQUEST).expectBody().json(
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
      @Sql("classpath:resource/healthandmedication/catering_instructions.sql")
      @Sql("classpath:resource/healthandmedication/field_metadata.sql")
      @Sql("classpath:resource/healthandmedication/field_history.sql")
      fun `can update existing diet and allergy data to empty lists and null values`() {
        expectSuccessfulUpdateFrom(
          // language=json
          """
            { "foodAllergies": [], "medicalDietaryRequirements": [], "personalisedDietaryRequirements": [], "cateringInstructions": null }
          """.trimIndent(),
        ).expectBody().json(
          // language=json
          """
            {
              "foodAllergies": {
                "value": [], 
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1",
                "lastModifiedPrisonId": "MDI"
              },
              "medicalDietaryRequirements": {
                "value": [], 
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1",
                "lastModifiedPrisonId": "MDI"
              },
              "personalisedDietaryRequirements": {
                "value": [], 
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1",
                "lastModifiedPrisonId": "MDI"
              },
              "cateringInstructions": {
                "value": null, 
                "lastModifiedAt":"2024-06-14T09:10:11+0100",
                "lastModifiedBy":"USER1",
                "lastModifiedPrisonId": "MDI"
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

  @DisplayName("PUT /prisoners/{prisonerNumber}/smoker")
  @Nested
  inner class UpdateSmokerStatusTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/smoker")
          .header("Content-Type", "application/json")
          .bodyValue(VALID_SMOKER_STATUS_REQUEST)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/smoker")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_SMOKER_STATUS_REQUEST)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/smoker")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .header("Content-Type", "application/json")
          .bodyValue(VALID_SMOKER_STATUS_REQUEST)
          .exchange()
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
          requestBody = """{ "smokerStatus": [] }""",
          message = "Validation failure: Couldn't read request body",
        )
      }

      @Test
      fun `bad request when incorrect domain for smoker status`() {
        expectBadRequestFrom(
          PRISONER_NUMBER,
          // language=json
          requestBody = """
          { 
            "smokerStatus": "MEDICAL_DIET_COELIAC"
          }
          """.trimMargin(),
          message = "Validation failure(s): The supplied code must either be null or match a valid reference data code of the correct domain.",
        )
      }

      private fun expectBadRequestFrom(prisonerNumber: String, requestBody: String, message: String) {
        webTestClient.put().uri("/prisoners/$prisonerNumber/smoker")
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
      fun `can update smoker status`() {
        webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/smoker")
          .headers(
            setAuthorisation(
              USER1,
              roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW"),
            ),
          )
          .header("Content-Type", "application/json")
          .bodyValue(VALID_SMOKER_STATUS_REQUEST)
          .exchange()
          .expectStatus().isNoContent
      }
    }
  }

  @Nested
  inner class PassUsernameInContextToPrisonApi {

    @BeforeEach
    fun setup() {
      HmppsAuthApiExtension.hmppsAuth.resetAll()
    }

    @Test
    fun `username is added to the oauth token and cached per user when updating language preferences`() {
      val listOfTestUsers = listOf("user1", "user2", "user3", "user4")
      val numberOfRepeatRequestsPerUser = 2

      for (i in 1..numberOfRepeatRequestsPerUser) {
        for (user in listOfTestUsers) {
          // The HMPPS Auth Token Endpoint stub will only match a request containing the provided
          // username in the request body.
          HmppsAuthApiExtension.hmppsAuth.stubUsernameEnhancedGrantToken(user)

          webTestClient.put().uri("/prisoners/${PRISONER_NUMBER}/smoker")
            .headers(
              setAuthorisation(
                user,
                roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW"),
              ),
            )
            .header("Content-Type", "application/json")
            .bodyValue(VALID_SMOKER_STATUS_REQUEST)
            .exchange()
            .expectStatus().isNoContent
        }
      }

      // There should be one request to the token endpoint for each unique user.
      HmppsAuthApiExtension.hmppsAuth.assertNumberStubGrantTokenCalls(listOfTestUsers.size)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val USER1 = "USER1"
    val NOW = ZonedDateTime.now(clock)
    val THEN = ZonedDateTime.of(2024, 1, 2, 9, 10, 11, 123000000, ZoneId.of("Europe/London"))

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

    val VALID_SMOKER_STATUS_REQUEST =
      // language=json
      """
        { 
          "smokerStatus": "SMOKER_YES"
        }
      """.trimIndent()
  }
}
