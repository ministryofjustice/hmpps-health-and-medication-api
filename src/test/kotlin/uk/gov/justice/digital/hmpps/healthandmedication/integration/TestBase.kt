package uk.gov.justice.digital.hmpps.healthandmedication.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.justice.digital.hmpps.healthandmedication.config.FixedClock
import uk.gov.justice.digital.hmpps.healthandmedication.integration.testcontainers.PostgresContainer
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.FieldHistoryRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.FieldMetadataRepository
import java.time.Instant
import java.time.ZoneId

@ActiveProfiles("test")
abstract class TestBase {

  @Autowired
  lateinit var fieldHistoryRepository: FieldHistoryRepository

  @Autowired
  lateinit var fieldMetadataRepository: FieldMetadataRepository

  companion object {
    val clock: FixedClock = FixedClock(
      Instant.parse("2024-06-14T09:10:11.123+01:00"),
      ZoneId.of("Europe/London"),
    )

    private val pgContainer = PostgresContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer?.run {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl)
        registry.add("spring.flyway.url", pgContainer::getJdbcUrl)
        registry.add("spring.flyway.user", pgContainer::getUsername)
        registry.add("spring.flyway.password", pgContainer::getPassword)
      }
    }
  }
}
