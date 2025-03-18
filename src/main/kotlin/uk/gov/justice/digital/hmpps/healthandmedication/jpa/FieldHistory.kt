package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.Hibernate
import uk.gov.justice.digital.hmpps.healthandmedication.enums.FieldValues
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import java.time.ZonedDateTime

@Entity
class FieldHistory(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val fieldHistoryId: Long = 0,

  // Allow this to mutate in order to handle merges
  var prisonerNumber: String,

  @Enumerated(STRING)
  @Column(updatable = false, nullable = false)
  val field: HealthAndMedicationField,

  override var valueInt: Int? = null,
  override var valueString: String? = null,

  @Convert(converter = JsonObjectConverter::class)
  override var valueJson: JsonObject? = null,

  @ManyToOne
  @JoinColumn(name = "valueRef", referencedColumnName = "id")
  override var valueRef: ReferenceDataCode? = null,

  override var prisonId: String,
  override val createdAt: ZonedDateTime = ZonedDateTime.now(),
  override val createdBy: String,
  override var mergedAt: ZonedDateTime? = null,
  override var mergedFrom: String? = null,

) : FieldValues,
  HistoryItem {

  fun toMetadata() = FieldMetadata(
    prisonerNumber = prisonerNumber,
    field = field,
    lastModifiedAt = createdAt,
    lastModifiedBy = createdBy,
    lastModifiedPrisonId = prisonId,
  )

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as FieldHistory

    if (prisonerNumber != other.prisonerNumber) return false
    if (field != other.field) return false
    if (valueInt != other.valueInt) return false
    if (valueString != other.valueString) return false
    if (valueRef != other.valueRef) return false
    if (valueJson != other.valueJson) return false
    if (prisonId != other.prisonId) return false
    if (createdAt != other.createdAt) return false
    if (createdBy != other.createdBy) return false
    if (mergedAt != other.mergedAt) return false
    if (mergedFrom != other.mergedFrom) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + field.hashCode()
    result = 31 * result + (valueInt ?: 0)
    result = 31 * result + (valueString?.hashCode() ?: 0)
    result = 31 * result + (valueRef?.hashCode() ?: 0)
    result = 31 * result + (valueJson?.hashCode() ?: 0)
    result = 31 * result + (prisonId.hashCode())
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + createdBy.hashCode()
    result = 31 * result + (mergedAt?.hashCode() ?: 0)
    result = 31 * result + (mergedFrom?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String = "FieldHistory(" +
    "fieldHistoryId=$fieldHistoryId, " +
    "prisonerNumber='$prisonerNumber', " +
    "field=$field, " +
    "valueInt=$valueInt, " +
    "valueString=$valueString, " +
    "valueRef=$valueRef, " +
    "valueJson=$valueJson, " +
    "prisonId='$prisonId', " +
    "createdAt=$createdAt, " +
    "createdBy='$createdBy', " +
    "mergedAt=$mergedAt, " +
    "mergedFrom=$mergedFrom, " +
    ")"
}
