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
  fun getReferenceDataCodes(domain: String, includeInactive: Boolean): Collection<ReferenceDataCodeDto> =
    referenceDataCodeRepository.findAllByDomainAndIncludeInactive(domain, includeInactive)
      .map { it.toDto() }

  fun getReferenceDataCode(code: String, domain: String): ReferenceDataCodeDto =
    referenceDataCodeRepository.findByCodeAndDomainCode(code, domain)?.toDto()
      ?: throw ReferenceDataCodeNotFoundException(code, domain)
}
