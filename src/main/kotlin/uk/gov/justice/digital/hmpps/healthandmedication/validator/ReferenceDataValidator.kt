package uk.gov.justice.digital.hmpps.healthandmedication.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.ReferenceDataValidation
import uk.gov.justice.digital.hmpps.healthandmedication.dto.request.ReferenceDataIdSelection
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository

@Service
class ReferenceDataValidator(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) : ConstraintValidator<ReferenceDataValidation, List<ReferenceDataIdSelection>> {

  private var validDomains = emptyList<String>()

  override fun initialize(constraintAnnotation: ReferenceDataValidation) {
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
