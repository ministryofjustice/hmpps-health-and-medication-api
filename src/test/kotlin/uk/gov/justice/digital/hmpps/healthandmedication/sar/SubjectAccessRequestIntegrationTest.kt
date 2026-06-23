package uk.gov.justice.digital.hmpps.healthandmedication.sar

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiDataTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarFlywaySchemaTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelperConfig
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarJpaEntitiesTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarReportTest
import java.time.ZonedDateTime
import javax.sql.DataSource

@Import(SarIntegrationTestHelperConfig::class)
class SubjectAccessRequestIntegrationTest :
  IntegrationTestBase(),
  SarFlywaySchemaTest,
  SarJpaEntitiesTest,
  SarApiDataTest,
  SarReportTest {

  @Autowired
  lateinit var dataSource: DataSource

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  override fun getSarHelper(): SarIntegrationTestHelper = sarIntegrationTestHelper

  override fun getWebTestClientInstance(): WebTestClient = webTestClient

  override fun getDataSourceInstance(): DataSource = dataSource

  override fun getEntityManagerInstance(): EntityManager = entityManager

  override fun getPrn(): String? = PRISONER_NUMBER

  override fun setupTestData() {} // Test data set up via sql annotations below:

  @Test
  @Sql("classpath:jpa/repository/reset.sql")
  @Sql("classpath:resource/healthandmedication/health.sql")
  @Sql("classpath:resource/healthandmedication/food_allergies.sql")
  @Sql("classpath:resource/healthandmedication/medical_dietary_requirements.sql")
  @Sql("classpath:resource/healthandmedication/personalised_dietary_requirements.sql")
  @Sql("classpath:resource/healthandmedication/catering_instructions.sql")
  @Sql("classpath:resource/healthandmedication/field_metadata.sql")
  @Sql("classpath:resource/healthandmedication/field_history.sql")
  override fun `SAR API should return expected data`() {
    super.`SAR API should return expected data`()
  }

  @Nested
  inner class PendingMerges {
    @BeforeEach
    fun setup() {
      fieldHistoryRepository.deleteAll()
      prisonerHealthRepository.deleteAll()

      prisonerHealthRepository.save(PrisonerHealth(prisonerNumber = PRISONER_NUMBER))
      prisonerHealthRepository.save(
        PrisonerHealth(
          prisonerNumber = PENDING_PRISONER_NUMBER,
          pendingMergeToPrisonerNumber = PRISONER_NUMBER,
        ),
      )

      fieldHistoryRepository.save(
        FieldHistory(
          prisonerNumber = PRISONER_NUMBER,
          field = HealthAndMedicationField.CATERING_INSTRUCTIONS,
          valueString = "Main record instructions",
          prisonId = "MDI",
          createdAt = ZonedDateTime.parse("2025-01-01T10:00:00Z"),
          createdBy = "USER1",
        ),
      )

      fieldHistoryRepository.save(
        FieldHistory(
          prisonerNumber = PENDING_PRISONER_NUMBER,
          field = HealthAndMedicationField.CATERING_INSTRUCTIONS,
          valueString = "Pending record instructions",
          prisonId = "MDI",
          createdAt = ZonedDateTime.parse("2025-01-02T10:00:00Z"),
          createdBy = "USER1",
        ),
      )
    }

    @Test
    fun `should return data from both a main record and its pending merge record when requesting the main PRN`() {
      // B -> A
      webTestClient.get().uri("/subject-access-request?prn=$PRISONER_NUMBER") // A
        .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.content.length()").isEqualTo(2)
        .jsonPath("$.content[0].prisonerNumber").isEqualTo(PENDING_PRISONER_NUMBER) // Most recent first
        .jsonPath("$.content[0].fieldHistoryValue").isEqualTo("Pending record instructions")
        .jsonPath("$.content[1].prisonerNumber").isEqualTo(PRISONER_NUMBER)
        .jsonPath("$.content[1].fieldHistoryValue").isEqualTo("Main record instructions")
    }

    @Test
    fun `should return data from both a main record and its pending merge record when requesting the pending PRN`() {
      // B -> A
      webTestClient.get().uri("/subject-access-request?prn=$PENDING_PRISONER_NUMBER") // B
        .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.content.length()").isEqualTo(2)
        .jsonPath("$.content[0].prisonerNumber").isEqualTo(PENDING_PRISONER_NUMBER)
        .jsonPath("$.content[1].prisonerNumber").isEqualTo(PRISONER_NUMBER)
    }
  }

  @Test
  fun `should not return data when requesting a non-existent PRN`() {
    webTestClient.get().uri("/subject-access-request?prn=NON_EXISTENT_PRN")
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isNoContent
      .expectBody().isEmpty
  }

  @Test
  fun `should not return data when requesting a PRN which falls outside of the requested date period`() {
    webTestClient.get().uri("/subject-access-request?prn=$PRISONER_NUMBER&fromDate=2023-01-01&toDate=2023-12-31")
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isNoContent
      .expectBody().isEmpty
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val PENDING_PRISONER_NUMBER = "B1234BB"
  }
}
