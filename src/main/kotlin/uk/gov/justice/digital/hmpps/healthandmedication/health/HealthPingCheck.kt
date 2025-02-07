package uk.gov.justice.digital.hmpps.healthandmedication.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.HealthPingCheck

@Component("hmppsAuth")
class HealthPingCheck(@Qualifier("hmppsAuthHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("prisonerSearch")
class PrisonerSearchHealthPing(@Qualifier("prisonerSearchHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("prisonApi")
class PrisonApiHealthPing(@Qualifier("prisonApiHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)
