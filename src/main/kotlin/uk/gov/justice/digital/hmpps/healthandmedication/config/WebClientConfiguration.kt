package uk.gov.justice.digital.hmpps.healthandmedication.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import uk.gov.justice.hmpps.kotlin.auth.usernameAwareTokenRequestOAuth2AuthorizedClientManager
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @param:Value("\${api.hmpps-auth.base-url}") private val hmppsAuthBaseUri: String,
  @param:Value("\${api.hmpps-auth.health-timeout:20s}") private val hmppsAuthHealthTimeout: Duration,

  @param:Value("\${api.prisoner-search.base-url}") private val prisonerSearchBaseUri: String,
  @param:Value("\${api.prisoner-search.timeout:30s}") private val prisonerSearchTimeout: Duration,
  @param:Value("\${api.prisoner-search.health-timeout:20s}") private val prisonerSearchHealthTimeout: Duration,

  @param:Value("\${api.prison-api.base-url}") private val prisonApiBaseUri: String,
  @param:Value("\${api.prison-api.timeout:30s}") private val prisonApiTimeout: Duration,
  @param:Value("\${api.prison-api.health-timeout:20s}") private val prisonApiHealthTimeout: Duration,
) {
  @Bean
  fun hmppsAuthHealthWebClient(builder: Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, hmppsAuthHealthTimeout)

  @Bean
  fun prisonerSearchHealthWebClient(builder: Builder) = builder.healthWebClient(prisonerSearchBaseUri, prisonerSearchHealthTimeout)

  @Bean
  fun prisonApiHealthWebClient(builder: Builder): WebClient = builder.healthWebClient(prisonApiBaseUri, prisonApiHealthTimeout)

  @Bean
  fun prisonerSearchWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = builder.authorisedWebClient(
    authorizedClientManager,
    "hmpps-health-and-medication-api",
    prisonerSearchBaseUri,
    prisonerSearchTimeout,
  )

  @Bean
  @RequestScope
  fun prisonApiWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
    builder: Builder,
  ) = builder.authorisedWebClient(
    usernameAwareTokenRequestOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService, Duration.ofSeconds(30)),
    "hmpps-health-and-medication-api",
    prisonApiBaseUri,
    prisonApiTimeout,
  )
}
