package uk.gov.justice.digital.hmpps.healthandmedication.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISON_ID

class PrisonsResourceIntTest : IntegrationTestBase() {

  @DisplayName("GET /prisons/{prisonId}/filters")
  @Nested
  inner class GetHealthAndMedicationFilters {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/prisons/${PRISON_ID}/filters")
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/prisons/${PRISON_ID}/filters")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/prisons/${PRISON_ID}/filters")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:resource/healthandmedication/health.sql")
      @Sql("classpath:resource/healthandmedication/food_allergies.sql")
      @Sql("classpath:resource/healthandmedication/medical_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/personalised_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/field_metadata.sql")
      @Sql("classpath:resource/healthandmedication/field_history.sql")
      fun `can get available filters and counts`() {
        val response = getAvailableFilters()

        response.expectBody().json(
          """
          {
            "foodAllergies": [
              {
                "name": "Soya",
                "value": "SOYA",
                "count": 2
              }
            ],
            "medicalDietaryRequirements": [
              {
                "name": "Low cholesterol",
                "value": "LOW_CHOLESTEROL",
                "count": 1
              }
            ],
            "personalisedDietaryRequirements": [
              {
                "name": "Kosher",
                "value": "KOSHER",
                "count": 1
              }
            ]
          }
          """.trimIndent(),
        )
      }

      private fun getAvailableFilters(): WebTestClient.ResponseSpec = webTestClient.get().uri("/prisons/${PRISON_ID}/filters")
        .headers(
          setAuthorisation(USER1, roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO")),
        )
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isOk
    }
  }

  @DisplayName("POST /prisons/{prisonId}")
  @Nested
  inner class GetBulkHealthAndMedicationDataForPrison {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.post().uri("/prisons/${PRISON_ID}")
          .header("Content-Type", "application/json")
          .bodyValue(BASIC_BULK_HEALTH_REQUEST)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.post().uri("/prisons/${PRISON_ID}")
          .headers(setAuthorisation(roles = listOf()))
          .header("Content-Type", "application/json")
          .bodyValue(BASIC_BULK_HEALTH_REQUEST)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.post().uri("/prisons/${PRISON_ID}")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .header("Content-Type", "application/json")
          .bodyValue(BASIC_BULK_HEALTH_REQUEST)
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      @Sql("classpath:jpa/repository/reset.sql")
      @Sql("classpath:resource/healthandmedication/health.sql")
      @Sql("classpath:resource/healthandmedication/food_allergies.sql")
      @Sql("classpath:resource/healthandmedication/medical_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/personalised_dietary_requirements.sql")
      @Sql("classpath:resource/healthandmedication/field_metadata.sql")
      @Sql("classpath:resource/healthandmedication/field_history.sql")
      fun `can get bulk health data for prison`() {
        val response = getBulkHealthData(BASIC_BULK_HEALTH_REQUEST)

        response.expectBody().json(
          """
          {
            "content": [
              {
                "prisonerNumber": "A1234AA",
                "health": {
                  "dietAndAllergy": {
                    "foodAllergies": {
                      "value": [
                        {
                          "value": {
                            "id": "FOOD_ALLERGY_SOYA",
                            "code": "SOYA",
                            "description": "Soya"
                          },
                          "comment" : null
                        }
                      ],
                      "lastModifiedAt": "2024-01-02T09:10:11+0000",
                      "lastModifiedBy": "USER1",
                      "lastModifiedPrisonId": "STI"
                    },
                    "medicalDietaryRequirements": {
                      "value": [
                        {
                          "value": {
                            "id": "MEDICAL_DIET_LOW_CHOLESTEROL",
                            "code": "LOW_CHOLESTEROL",
                            "description": "Low cholesterol"
                          },
                          "comment" : null 
                        } 
                      ],
                      "lastModifiedAt": "2024-01-02T09:10:11+0000",
                      "lastModifiedBy": "USER1",
                      "lastModifiedPrisonId": "STI"
                    },
                    "personalisedDietaryRequirements": {
                      "value": [
                        {
                          "value": {
                            "id": "PERSONALISED_DIET_KOSHER",
                            "code": "KOSHER",
                            "description": "Kosher"
                          },
                          "comment" : null 
                        } 
                      ],
                      "lastModifiedAt": "2024-01-02T09:10:11+0000",
                      "lastModifiedBy": "USER1",
                      "lastModifiedPrisonId": "STI"
                    }
                  }
                }
              },
              {
                "prisonerNumber": "B1234CC",
                "health": {
                  "dietAndAllergy": {
                    "foodAllergies": {
                      "value": [
                        {
                          "value": {
                            "id": "FOOD_ALLERGY_SOYA",
                            "code": "SOYA",
                            "description": "Soya"
                          },
                          "comment" : null
                        }
                      ],
                      "lastModifiedAt": "2024-01-02T09:10:11+0000",
                      "lastModifiedBy": "USER1",
                      "lastModifiedPrisonId": "STI"
                    }
                  }
                }
              }
            ],
            "metadata": {
              "first": true,
              "last": true,
              "numberOfElements": 2,
              "pageNumber": 1,
              "size": 20,
              "offset": 0,
              "totalElements": 2,
              "totalPages": 1
            }
          }
          """.trimIndent(),
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
      fun `can apply filters`() {
        val response = getBulkHealthData(FILTERED_BULK_HEALTH_REQUEST)

        response.expectBody().json(
          """
          {
            "content": [
              {
                "prisonerNumber": "A1234AA",
                "health": {
                  "dietAndAllergy": {
                    "foodAllergies": {
                      "value": [
                        {
                          "value": {
                            "id": "FOOD_ALLERGY_SOYA",
                            "code": "SOYA",
                            "description": "Soya"
                          },
                          "comment" : null
                        }
                      ],
                      "lastModifiedAt": "2024-01-02T09:10:11+0000",
                      "lastModifiedBy": "USER1",
                      "lastModifiedPrisonId": "STI"
                    },
                    "medicalDietaryRequirements": {
                      "value": [
                        {
                          "value": {
                            "id": "MEDICAL_DIET_LOW_CHOLESTEROL",
                            "code": "LOW_CHOLESTEROL",
                            "description": "Low cholesterol"
                          },
                          "comment" : null 
                        } 
                      ],
                      "lastModifiedAt": "2024-01-02T09:10:11+0000",
                      "lastModifiedBy": "USER1",
                      "lastModifiedPrisonId": "STI"
                    },
                    "personalisedDietaryRequirements": {
                      "value": [
                        {
                          "value": {
                            "id": "PERSONALISED_DIET_KOSHER",
                            "code": "KOSHER",
                            "description": "Kosher"
                          },
                          "comment" : null 
                        } 
                      ],
                      "lastModifiedAt": "2024-01-02T09:10:11+0000",
                      "lastModifiedBy": "USER1",
                      "lastModifiedPrisonId": "STI"
                    }
                  }
                }
              }
            ],
            "metadata": {
              "first": true,
              "last": true,
              "numberOfElements": 1,
              "pageNumber": 1,
              "size": 20,
              "offset": 0,
              "totalElements": 1,
              "totalPages": 1
            }
          }
          """.trimIndent(),
        )
      }

      private fun getBulkHealthData(request: String): WebTestClient.ResponseSpec = webTestClient.post()
        .uri("/prisons/${PRISON_ID}")
        .headers(
          setAuthorisation(USER1, roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO")),
        )
        .header("Content-Type", "application/json")
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
    }
  }

  private companion object {
    const val USER1 = "USER1"

    val BASIC_BULK_HEALTH_REQUEST =
      // language=json
      """
        { 
          "page": 1,
          "size": 20
        }
      """.trimIndent()

    val FILTERED_BULK_HEALTH_REQUEST =
      // language=json
      """
        { 
          "page": 1,
          "size": 20,
          "filters": {
            "personalisedDietaryRequirements": ["KOSHER"]
          }
        }
      """.trimIndent()
  }
}
