package uk.gov.justice.digital.hmpps.healthandmedication.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.PrisonApiClient

@Configuration
class ClientConfiguration {

  @Primary
  @Bean
  fun prisonApiClient(@Qualifier("usernameAwarePrisonApiWebClient") webClient: WebClient): PrisonApiClient = PrisonApiClient(webClient)

  @Bean
  fun systemPrisonApiClient(@Qualifier("prisonApiWebClient") webClient: WebClient): PrisonApiClient = PrisonApiClient(webClient)
}
