package uk.gov.justice.digital.hmpps.healthandmedication.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.ReferenceDataValidation
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository

@Service
class ReferenceDataValidator(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) : ConstraintValidator<ReferenceDataValidation, String> {

  private var validDomains = emptyList<String>()

  override fun initialize(constraintAnnotation: ReferenceDataValidation) {
    this.validDomains = constraintAnnotation.domains.toList()
  }

  override fun isValid(
    value: String?,
    context: ConstraintValidatorContext?,
  ): Boolean = if (value == null) {
    true
  } else {
    val validCodes = validDomains.flatMap {
      referenceDataCodeRepository.findAllByDomainAndIncludeInactive(
        domain = it,
        includeInactive = false,
      )
    }.map { it.id }

    validCodes.contains(value)
  }
}
