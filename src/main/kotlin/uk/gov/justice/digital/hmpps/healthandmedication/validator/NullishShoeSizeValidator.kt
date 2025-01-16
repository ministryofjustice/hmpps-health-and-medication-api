package uk.gov.justice.digital.hmpps.healthandmedication.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.NullishShoeSize
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish.Defined
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish.Undefined

class NullishShoeSizeValidator : ConstraintValidator<NullishShoeSize, Nullish<out String>> {

  private var min: String = "0"
  private var max: String = "0"

  override fun initialize(constraintAnnotation: NullishShoeSize) {
    this.min = constraintAnnotation.min
    this.max = constraintAnnotation.max
  }

  override fun isValid(value: Nullish<out String>, context: ConstraintValidatorContext?): Boolean = when (value) {
    Undefined -> true
    is Defined -> value.value == null || isValidShoeSize(value.value)
  }

  private fun isValidShoeSize(shoeSize: String): Boolean {
    val validShowSize = """^([1-9]|1[0-9]|2[0-5])(\.5|\.0)?$""".toRegex()
    if (!shoeSize.matches(validShowSize)) {
      return false
    }

    val number = shoeSize.toDouble()
    return number in 1.0..25.0
  }
}
