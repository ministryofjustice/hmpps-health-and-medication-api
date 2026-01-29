package uk.gov.justice.digital.hmpps.healthandmedication.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.healthandmedication.integration.TestBase.Companion.clock
import java.time.Clock

@TestConfiguration
class FixedClockConfig {
  @Primary
  @Bean
  fun fixedClock(): Clock = clock
}
