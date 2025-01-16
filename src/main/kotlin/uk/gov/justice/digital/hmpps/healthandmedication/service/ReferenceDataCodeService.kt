package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.config.ReferenceDataCodeNotFoundException
import uk.gov.justice.digital.hmpps.healthandmedication.dto.ReferenceDataCodeDto
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.mapper.toDto

@Service
@Transactional(readOnly = true)
class ReferenceDataCodeService(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) {

  /*
   * Exclude certain reference data codes
   */
  private val excludedCodes = setOf(
    Pair("FACIAL_HAIR", "NA"),
    Pair("EYE", "MISSING"),
  )

  fun getReferenceDataCodes(domain: String, includeInactive: Boolean): Collection<ReferenceDataCodeDto> =
    referenceDataCodeRepository.findAllByDomainAndIncludeInactive(domain, includeInactive)
      .filterNot { excludedCodes.contains(Pair(it.domain.code, it.code)) }
      .map { it.toDto() }

  fun getReferenceDataCode(code: String, domain: String): ReferenceDataCodeDto =
    referenceDataCodeRepository.findByCodeAndDomainCode(code, domain)?.toDto()
      ?: throw ReferenceDataCodeNotFoundException(code, domain)
}
