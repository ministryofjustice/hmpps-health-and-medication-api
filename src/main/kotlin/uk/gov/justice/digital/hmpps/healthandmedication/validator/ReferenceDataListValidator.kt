package uk.gov.justice.digital.hmpps.healthandmedication.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.ReferenceDataListValidation
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.ReferenceDataIdSelection

@Service
class ReferenceDataListValidator(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) : ConstraintValidator<ReferenceDataListValidation, List<ReferenceDataIdSelection>> {

  private var validDomains = emptyList<String>()

  override fun initialize(constraintAnnotation: ReferenceDataListValidation) {
    this.validDomains = constraintAnnotation.domains.toList()
  }

  override fun isValid(
    value: List<ReferenceDataIdSelection>?,
    context: ConstraintValidatorContext?,
  ): Boolean = if (value == null) {
    false
  } else {
    val validCodes = validDomains.flatMap {
      referenceDataCodeRepository.findAllByDomainAndIncludeInactive(
        domain = it,
        includeInactive = false,
      )
    }.map { it.id }

    validCodes.containsAll(value.map { it.value })
  }
}
