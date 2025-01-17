package uk.gov.justice.digital.hmpps.healthandmedication.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.hmpps-auth.base-url}") private val hmppsAuthBaseUri: String,
  @Value("\${api.hmpps-auth.health-timeout:20s}") private val hmppsAuthHealthTimeout: Duration,

  @Value("\${api.prisoner-search.base-url}") private val prisonerSearchBaseUri: String,
  @Value("\${api.prisoner-search.timeout:30s}") private val prisonerSearchTimeout: Duration,
  @Value("\${api.prisoner-search.health-timeout:20s}") private val prisonerSearchHealthTimeout: Duration,
) {
  @Bean
  fun hmppsAuthHealthWebClient(builder: Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, hmppsAuthHealthTimeout)

  @Bean
  fun prisonerSearchHealthWebClient(builder: Builder) = builder.healthWebClient(prisonerSearchBaseUri, prisonerSearchHealthTimeout)

  @Bean
  fun prisonerSearchWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = builder.authorisedWebClient(
    authorizedClientManager,
    "hmpps-health-and-medication-api",
    prisonerSearchBaseUri,
    prisonerSearchTimeout,
  )
}
