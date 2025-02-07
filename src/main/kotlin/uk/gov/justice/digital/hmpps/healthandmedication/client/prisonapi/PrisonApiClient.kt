package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatusUpdate
import uk.gov.justice.digital.hmpps.healthandmedication.config.DownstreamServiceException

@Component
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {
  fun updateSmokerStatus(offenderNo: String, updateSmokerStatus: PrisonApiSmokerStatusUpdate) = try {
    webClient
      .put()
      .uri("/api/offenders/{offenderNo}/smoker", offenderNo)
      .bodyValue(updateSmokerStatus)
      .retrieve()
      .toBodilessEntity()
      .block()
  } catch (e: Exception) {
    throw DownstreamServiceException("Update smoker status request failed", e)
  }
}
