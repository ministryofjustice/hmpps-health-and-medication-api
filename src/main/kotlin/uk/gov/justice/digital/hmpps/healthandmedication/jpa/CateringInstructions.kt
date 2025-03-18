package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.Hibernate

@Entity
class CateringInstructions(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @Column(name = "instructions")
  val instructions: String?,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as CateringInstructions

    if (prisonerNumber != other.prisonerNumber) return false
    if (instructions != other.instructions) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + instructions.hashCode()
    return result
  }

  override fun toString(): String = "CateringInstructions(prisonerNumber='$prisonerNumber', instructions=$instructions)"
}
