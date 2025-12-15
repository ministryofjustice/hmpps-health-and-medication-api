package uk.gov.justice.digital.hmpps.healthandmedication.sar

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiDataTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarFlywaySchemaTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelperConfig
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarJpaEntitiesTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarReportTest
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

  override fun getPrn(): String? = "A1234AA"

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
}
