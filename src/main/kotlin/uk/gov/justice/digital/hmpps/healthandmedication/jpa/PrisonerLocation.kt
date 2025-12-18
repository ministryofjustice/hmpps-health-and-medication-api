package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
class PrisonerLocation(

  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @Column(name = "prison_id")
  val prisonId: String? = null,

  @Column(name = "l1_location")
  val topLocationLevel: String? = null,

  val location: String? = null,

  @Column(name = "last_admission_date")
  val lastAdmissionDate: LocalDate? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PrisonerLocation

    if (prisonerNumber != other.prisonerNumber) return false
    if (prisonId != other.prisonId) return false
    if (topLocationLevel != other.topLocationLevel) return false
    if (location != other.location) return false
    if (lastAdmissionDate != other.lastAdmissionDate) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + (prisonId?.hashCode() ?: 0)
    result = 31 * result + (topLocationLevel?.hashCode() ?: 0)
    result = 31 * result + (location?.hashCode() ?: 0)
    result = 31 * result + (lastAdmissionDate?.hashCode() ?: 0)
    return result
  }
}
