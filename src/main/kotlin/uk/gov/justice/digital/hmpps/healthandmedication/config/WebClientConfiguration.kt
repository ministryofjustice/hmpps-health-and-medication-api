package uk.gov.justice.digital.hmpps.healthandmedication.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import uk.gov.justice.digital.hmpps.personintegrationapi.config.UserEnhancedOAuth2ClientCredentialGrantRequestConverter
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

  @Value("\${api.prison-api.base-url}") private val prisonApiBaseUri: String,
  @Value("\${api.prison-api.timeout:30s}") private val prisonApiTimeout: Duration,
  @Value("\${api.prison-api.health-timeout:20s}") private val prisonApiHealthTimeout: Duration,
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
    authorizedClientManagerUserEnhanced(clientRegistrationRepository, oAuth2AuthorizedClientService),
    "hmpps-health-and-medication-api",
    prisonApiBaseUri,
    prisonApiTimeout,
  )

  private fun authorizedClientManagerUserEnhanced(clients: ClientRegistrationRepository?, clientService: OAuth2AuthorizedClientService): OAuth2AuthorizedClientManager {
    val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, clientService)

    val defaultClientCredentialsTokenResponseClient = DefaultClientCredentialsTokenResponseClient()
    val authentication = SecurityContextHolder.getContext().authentication
    defaultClientCredentialsTokenResponseClient.setRequestEntityConverter { grantRequest: OAuth2ClientCredentialsGrantRequest ->
      val converter = UserEnhancedOAuth2ClientCredentialGrantRequestConverter()
      converter.enhanceWithUsername(grantRequest, authentication.name)
    }

    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials { clientCredentialsGrantBuilder: OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder ->
        clientCredentialsGrantBuilder.accessTokenResponseClient(
          defaultClientCredentialsTokenResponseClient,
        )
      }
      .build()

    manager.setAuthorizedClientProvider(authorizedClientProvider)
    return manager
  }
}
