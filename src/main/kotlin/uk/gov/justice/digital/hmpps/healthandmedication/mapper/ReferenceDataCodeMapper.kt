package uk.gov.justice.digital.hmpps.healthandmedication.mapper

import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.ReferenceDataCodeDto
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.ReferenceDataSimpleDto
import java.time.ZonedDateTime

fun ReferenceDataCode.toDto(): ReferenceDataCodeDto = ReferenceDataCodeDto(
  id,
  domain = domain.code,
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
)

fun ReferenceDataCode.toSimpleDto(): ReferenceDataSimpleDto = ReferenceDataSimpleDto(
  id,
  description,
  listSequence,
  isActive(),
)

fun ReferenceDataCode.isActive() = deactivatedAt?.isBefore(ZonedDateTime.now()) != true
