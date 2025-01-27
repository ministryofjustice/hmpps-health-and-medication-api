package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.dto.PrisonerDto
import uk.gov.justice.digital.hmpps.healthandmedication.config.DownstreamServiceException

data class PrisonerSearchResult(
  val content: List<PrisonerDto>,
)

@Component
class PrisonerSearchClient(@Qualifier("prisonerSearchWebClient") private val webClient: WebClient) {
  fun getPrisoner(prisonerNumber: String): PrisonerDto? = try {
    webClient
      .get()
      .uri("/prisoner/{prisonerNumber}", prisonerNumber)
      .retrieve()
      .bodyToMono(PrisonerDto::class.java)
      .block()
  } catch (e: NotFound) {
    null
  } catch (e: Exception) {
    throw DownstreamServiceException("Get prisoner request failed", e)
  }

  fun getPrisonersForPrison(prisonId: String, sort: String? = null): List<PrisonerDto>? {
    try {
      var query = "size=9999"
      if (!sort.isNullOrEmpty()) query += "&sort=$sort"

      return webClient.get().uri("/prison/{prisonId}/prisoners?$query", prisonId).retrieve()
        .bodyToMono(PrisonerSearchResult::class.java)
        .block()?.content
    } catch (e: NotFound) {
      return null
    } catch (e: Exception) {
      throw DownstreamServiceException("Get prisoners for prison request failed", e)
    }
  }
}
