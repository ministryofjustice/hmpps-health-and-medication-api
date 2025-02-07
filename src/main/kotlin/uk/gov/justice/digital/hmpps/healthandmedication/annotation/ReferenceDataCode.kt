package uk.gov.justice.digital.hmpps.healthandmedication.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uk.gov.justice.digital.hmpps.healthandmedication.validator.ReferenceDataListValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ReferenceDataListValidator::class])
annotation class ReferenceDataCode(
  val domains: Array<String> = [],
  val message: String = "The value must be a reference domain code id of the correct domain, null, or Undefined.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
