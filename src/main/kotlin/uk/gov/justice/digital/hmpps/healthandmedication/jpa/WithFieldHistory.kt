package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import org.springframework.data.domain.AbstractAggregateRoot
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import java.time.ZonedDateTime
import java.util.SortedSet
import kotlin.reflect.KMutableProperty0

abstract class WithFieldHistory<T : AbstractAggregateRoot<T>?> : AbstractAggregateRoot<T>() {
  abstract val prisonerNumber: String
  abstract val fieldHistory: SortedSet<FieldHistory>
  abstract val fieldMetadata: MutableMap<HealthAndMedicationField, FieldMetadata>
  protected abstract fun fieldAccessors(): Map<HealthAndMedicationField, KMutableProperty0<*>>

  abstract fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
  ): Collection<HealthAndMedicationField>

  fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
    fields: Collection<HealthAndMedicationField>,
  ): Collection<HealthAndMedicationField> {
    val changedFields = mutableSetOf<HealthAndMedicationField>()

    fieldAccessors()
      .filter { fields.contains(it.key) }
      .forEach { (field, currentValue) ->
        val previousVersion = fieldHistory.lastOrNull { it.field == field }
        if (previousVersion == null || field.hasChangedFrom(previousVersion, currentValue())) {
          fieldMetadata[field] = FieldMetadata(
            field = field,
            prisonerNumber = this.prisonerNumber,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy,
          )

          fieldHistory.add(
            FieldHistory(
              prisonerNumber = this.prisonerNumber,
              field = field,
              createdAt = lastModifiedAt,
              createdBy = lastModifiedBy,
            ).also { field.set(it, currentValue()) },
          )
          changedFields.add(field)
        }
      }
    return changedFields
  }

  public override fun domainEvents(): Collection<Any> = super.domainEvents()
}
