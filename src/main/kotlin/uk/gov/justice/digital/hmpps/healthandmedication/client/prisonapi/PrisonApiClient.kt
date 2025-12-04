package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request.PrisonApiSmokerStatusUpdate
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.response.PrisonerHousingLocationDto
import uk.gov.justice.digital.hmpps.healthandmedication.config.DownstreamServiceException

class PrisonApiClient(private val webClient: WebClient) {
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

  fun getHousingLocation(offenderNo: String) = try {
    webClient
      .get()
      .uri("/api/offenders/{offenderNo}/housing-location", offenderNo)
      .retrieve()
      .bodyToMono(PrisonerHousingLocationDto::class.java)
      .block()
  } catch (e: NotFound) {
    null
  } catch (e: Exception) {
    throw DownstreamServiceException("Get housing location request failed", e)
  }
}
