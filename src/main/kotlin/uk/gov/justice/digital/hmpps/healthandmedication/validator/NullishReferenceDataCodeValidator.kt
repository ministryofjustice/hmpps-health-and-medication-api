package uk.gov.justice.digital.hmpps.healthandmedication.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.NullishReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish
import kotlin.jvm.optionals.getOrNull

@Service
class NullishReferenceDataCodeValidator(
  private val referenceDataCodeRepository: ReferenceDataCodeRepository,
) :
  ConstraintValidator<NullishReferenceDataCode, Nullish<out String>> {

  private var validDomains = emptyList<String>()

  private var allowNull = true

  override fun initialize(constraintAnnotation: NullishReferenceDataCode) {
    this.validDomains = constraintAnnotation.domains.toList()
    this.allowNull = constraintAnnotation.allowNull
  }

  override fun isValid(value: Nullish<out String>?, context: ConstraintValidatorContext?): Boolean =
    when (value) {
      Nullish.Undefined -> true
      is Nullish.Defined -> {
        value.value?.let {
          referenceDataCodeRepository.findById(it).getOrNull()?.domain?.code in validDomains
        } ?: allowNull
      }

      null -> false
    }
}
