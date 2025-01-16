package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import org.hibernate.Hibernate
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import java.io.Serializable
import java.time.ZonedDateTime

@Entity
@IdClass(FieldMetadataId::class)
class FieldMetadata(
  @Id
  @Column(updatable = false, nullable = false)
  val prisonerNumber: String,

  @Id
  @Enumerated(STRING)
  @Column(updatable = false, nullable = false)
  val field: HealthAndMedicationField,

  var lastModifiedAt: ZonedDateTime = ZonedDateTime.now(),

  var lastModifiedBy: String,
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as FieldMetadata

    if (prisonerNumber != other.prisonerNumber) return false
    if (field != other.field) return false
    if (lastModifiedAt.toInstant() != other.lastModifiedAt.toInstant()) return false
    if (lastModifiedBy != other.lastModifiedBy) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + field.hashCode()
    result = 31 * result + lastModifiedAt.toInstant().hashCode()
    result = 31 * result + lastModifiedBy.hashCode()
    return result
  }

  override fun toString(): String = "FieldMetadata(prisonerNumber='$prisonerNumber', field=$field, lastModifiedAt=$lastModifiedAt, lastModifiedBy='$lastModifiedBy')"
}

private data class FieldMetadataId(val prisonerNumber: String? = null, val field: HealthAndMedicationField? = null) : Serializable
