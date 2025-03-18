package uk.gov.justice.digital.hmpps.healthandmedication.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase

class ReferenceDataDomainResourceIntTest : IntegrationTestBase() {

  @DisplayName("GET /reference-data/domains")
  @Nested
  inner class GetReferenceDataDomainsTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data domains`() {
        webTestClient.get().uri("/reference-data/domains")
          .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.length()").isEqualTo(4)
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].description").isEqualTo("Food allergy")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].listSequence").isEqualTo(0)
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].isActive").isEqualTo(true)
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].createdAt").isEqualTo("2025-01-16T00:00:00+0000")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].createdBy").isEqualTo("CONNECT_DPS")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes.length()").isEqualTo(15)
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[0].id").isEqualTo("FOOD_ALLERGY_CELERY")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[1].id").isEqualTo("FOOD_ALLERGY_GLUTEN")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[2].id").isEqualTo("FOOD_ALLERGY_CRUSTACEANS")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[3].id").isEqualTo("FOOD_ALLERGY_EGG")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[4].id").isEqualTo("FOOD_ALLERGY_FISH")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[5].id").isEqualTo("FOOD_ALLERGY_LUPIN")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[6].id").isEqualTo("FOOD_ALLERGY_MILK")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[7].id").isEqualTo("FOOD_ALLERGY_MOLLUSCS")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[8].id").isEqualTo("FOOD_ALLERGY_MUSTARD")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[9].id").isEqualTo("FOOD_ALLERGY_PEANUTS")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[10].id").isEqualTo("FOOD_ALLERGY_SESAME")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[11].id").isEqualTo("FOOD_ALLERGY_SOYA")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[12].id")
          .isEqualTo("FOOD_ALLERGY_SULPHUR_DIOXIDE")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[13].id").isEqualTo("FOOD_ALLERGY_TREE_NUTS")
          .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].referenceDataCodes[14].id").isEqualTo("FOOD_ALLERGY_OTHER")
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].description").isEqualTo("Medical diet")
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].listSequence").isEqualTo(0)
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].isActive").isEqualTo(true)
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].createdAt").isEqualTo("2025-01-16T00:00:00+0000")
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].createdBy").isEqualTo("CONNECT_DPS")
          .jsonPath("$[?(@.code == 'MEDICAL_DIET')].referenceDataCodes.length()").isEqualTo(10)
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].description").isEqualTo("Personalised diet")
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].listSequence").isEqualTo(0)
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].isActive").isEqualTo(true)
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].createdAt").isEqualTo("2025-01-16T00:00:00+0000")
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].createdBy").isEqualTo("CONNECT_DPS")
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].referenceDataCodes.length()").isEqualTo(4)
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].referenceDataCodes[0].id").isEqualTo("PERSONALISED_DIET_HALAL")
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].referenceDataCodes[1].id").isEqualTo("PERSONALISED_DIET_KOSHER")
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].referenceDataCodes[2].id").isEqualTo("PERSONALISED_DIET_VEGAN")
          .jsonPath("$[?(@.code == 'PERSONALISED_DIET')].referenceDataCodes[3].id").isEqualTo("PERSONALISED_DIET_OTHER")
          .jsonPath("$[?(@.code == 'SMOKER')].description").isEqualTo("Smoker or vaper")
          .jsonPath("$[?(@.code == 'SMOKER')].listSequence").isEqualTo(0)
          .jsonPath("$[?(@.code == 'SMOKER')].isActive").isEqualTo(true)
          .jsonPath("$[?(@.code == 'SMOKER')].createdAt").isEqualTo("2025-02-07T00:00:00+0000")
          .jsonPath("$[?(@.code == 'SMOKER')].createdBy").isEqualTo("CONNECT_DPS")
          .jsonPath("$[?(@.code == 'SMOKER')].referenceDataCodes.length()").isEqualTo(3)
      }
    }

    @Test
    fun `can retrieve reference data domains including sub-domains at the top level`() {
      webTestClient.get().uri("/reference-data/domains?includeSubDomains=true")
        .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(4)
        .jsonPath("$[?(@.code == 'FOOD_ALLERGY')].isActive").isEqualTo(true)
    }
  }

  @DisplayName("GET /reference-data/domains/{domain}")
  @Nested
  inner class GetReferenceDataDomainTest {

    @Nested
    inner class Security {

      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY")
          .headers(setAuthorisation(roles = listOf("ROLE_IS_WRONG")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can retrieve reference data domain`() {
        webTestClient.get().uri("/reference-data/domains/FOOD_ALLERGY")
          .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
              {
                "code": "FOOD_ALLERGY",
                "description": "Food allergy",
                "listSequence": 0,
                "isActive": true,
                "createdAt": "2025-01-16T00:00:00+0000",
                "createdBy": "CONNECT_DPS",
                "referenceDataCodes": [
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
              }
            """.trimIndent(),
          )
      }
    }

    @Nested
    inner class NotFound {

      @Test
      fun `receive a 404 when no reference data domain found`() {
        webTestClient.get().uri("/reference-data/domains/UNKNOWN")
          .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO")))
          .exchange()
          .expectStatus().isNotFound
      }
    }
  }
}
