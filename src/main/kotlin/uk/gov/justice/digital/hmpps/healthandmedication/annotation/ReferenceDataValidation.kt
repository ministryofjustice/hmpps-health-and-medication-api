package uk.gov.justice.digital.hmpps.healthandmedication.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uk.gov.justice.digital.hmpps.healthandmedication.validator.ReferenceDataValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ReferenceDataValidator::class])
annotation class ReferenceDataValidation(
  val domains: Array<String> = [],
  val message: String = "The supplied array must either be empty or contain reference data codes of the correct domain.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
