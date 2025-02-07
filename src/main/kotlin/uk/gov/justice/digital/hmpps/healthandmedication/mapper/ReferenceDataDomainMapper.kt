package uk.gov.justice.digital.hmpps.healthandmedication.mapper

import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.ReferenceDataDomainDto
import java.time.ZonedDateTime

fun ReferenceDataDomain.toDto(): ReferenceDataDomainDto = ReferenceDataDomainDto(
  code,
  description,
  listSequence,
  isActive(),
  createdAt,
  createdBy,
  lastModifiedAt,
  lastModifiedBy,
  deactivatedAt,
  deactivatedBy,
  referenceDataCodes.map { it.toDto() },
  subDomains.map { it.toDto() },
)

fun ReferenceDataDomain.isActive() = deactivatedAt?.isBefore(ZonedDateTime.now()) != true
