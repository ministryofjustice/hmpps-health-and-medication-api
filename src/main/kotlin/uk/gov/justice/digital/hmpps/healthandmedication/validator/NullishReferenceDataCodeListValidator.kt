package uk.gov.justice.digital.hmpps.healthandmedication.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.NullishReferenceDataCodeList
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish

@Service
class NullishReferenceDataCodeListValidator(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) : ConstraintValidator<NullishReferenceDataCodeList, Nullish<out List<String>>> {

  private var validDomains = emptyList<String>()

  override fun initialize(constraintAnnotation: NullishReferenceDataCodeList) {
    this.validDomains = constraintAnnotation.domains.toList()
  }

  override fun isValid(value: Nullish<out List<String>>?, context: ConstraintValidatorContext?): Boolean =
    when (value) {
      Nullish.Undefined -> true
      is Nullish.Defined -> {
        if (value.value == null) {
          false
        } else {
          val validCodes = validDomains.flatMap {
            referenceDataCodeRepository.findAllByDomainAndIncludeInactive(
              domain = it,
              includeInactive = false,
            )
          }.map { it.id }
          validCodes.containsAll(value.value)
        }
      }

      null -> false
    }
}
