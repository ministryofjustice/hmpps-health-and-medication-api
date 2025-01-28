package uk.gov.justice.digital.hmpps.healthandmedication.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase

class ReferenceDataCodeResourceIntTest : IntegrationTestBase() {

  @DisplayName("GET /reference-data/domains/{domain}/codes")
  @Nested
  inner class GetReferenceDataCodesTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY/codes")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data codes`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY/codes")
          .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
            [
              {
                "id": "FOOD_ALLERGY_CELERY",
                "domain": "FOOD_ALLERGY",
                "code": "CELERY",
                "description": "Celery",
                "listSequence": 0,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_GLUTEN",
                "domain": "FOOD_ALLERGY",
                "code": "GLUTEN",
                "description": "Cereals containing gluten",
                "listSequence": 1,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_CRUSTACEANS",
                "domain": "FOOD_ALLERGY",
                "code": "CRUSTACEANS",
                "description": "Crustaceans",
                "listSequence": 2,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_EGG",
                "domain": "FOOD_ALLERGY",
                "code": "EGG",
                "description": "Egg",
                "listSequence": 3,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_FISH",
                "domain": "FOOD_ALLERGY",
                "code": "FISH",
                "description": "Fish",
                "listSequence": 4,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_LUPIN",
                "domain": "FOOD_ALLERGY",
                "code": "LUPIN",
                "description": "Lupin",
                "listSequence": 5,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_MILK",
                "domain": "FOOD_ALLERGY",
                "code": "MILK",
                "description": "Milk",
                "listSequence": 6,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_MOLLUSCS",
                "domain": "FOOD_ALLERGY",
                "code": "MOLLUSCS",
                "description": "Molluscs",
                "listSequence": 7,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_MUSTARD",
                "domain": "FOOD_ALLERGY",
                "code": "MUSTARD",
                "description": "Mustard",
                "listSequence": 8,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_PEANUTS",
                "domain": "FOOD_ALLERGY",
                "code": "PEANUTS",
                "description": "Peanuts",
                "listSequence": 9,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_SESAME",
                "domain": "FOOD_ALLERGY",
                "code": "SESAME",
                "description": "Sesame",
                "listSequence": 10,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_SOYA",
                "domain": "FOOD_ALLERGY",
                "code": "SOYA",
                "description": "Soya",
                "listSequence": 11,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_SULPHUR_DIOXIDE",
                "domain": "FOOD_ALLERGY",
                "code": "SULPHUR_DIOXIDE",
                "description": "Sulphur dioxide",
                "listSequence": 12,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_TREE_NUTS",
                "domain": "FOOD_ALLERGY",
                "code": "TREE_NUTS",
                "description": "Tree nuts",
                "listSequence": 13,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              },
              {
                "id": "FOOD_ALLERGY_OTHER",
                "domain": "FOOD_ALLERGY",
                "code": "OTHER",
                "description": "Other",
                "listSequence": 14,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS"
              }
            ]
            """.trimIndent(),
          )
      }
    }
  }

  @DisplayName("GET /reference-data/domains/{domain}/codes/{code}")
  @Nested
  inner class GetReferenceDataCodeTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY/codes/MILK")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY/codes/MILK")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data code`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY/codes/MILK")
          .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
            {
              "id": "FOOD_ALLERGY_MILK",
              "domain": "FOOD_ALLERGY",
              "code": "MILK",
              "description": "Milk",
              "listSequence": 6,
              "isActive": true,
              "createdAt": "2025-01-16T00:00:00+0000",
              "createdBy": "CONNECT_DPS"
            }
            """.trimIndent(),
          )
      }
    }

    @Nested
    inner class NotFound {

      @Test
      fun `receive a 404 when no reference data code found`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY/codes/UNKNOWN")
          .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isNotFound
      }
    }
  }
}
