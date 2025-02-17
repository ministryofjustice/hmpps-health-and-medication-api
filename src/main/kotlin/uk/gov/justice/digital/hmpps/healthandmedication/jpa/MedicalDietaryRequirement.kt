package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.Hibernate

@Entity
class MedicalDietaryRequirement(
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(
    name = "dietary_requirement",
    referencedColumnName = "id",
  ) var dietaryRequirement: ReferenceDataCode,

  var commentText: String? = null,
) {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  val id: Long = 0

  fun toHistoryObject() = MedicalDietaryRequirementItem(dietaryRequirement.id, commentText)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as MedicalDietaryRequirement

    if (prisonerNumber != other.prisonerNumber) return false
    if (dietaryRequirement != other.dietaryRequirement) return false
    if (commentText != other.commentText) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + dietaryRequirement.hashCode()
    result = 31 * result + commentText.hashCode()
    return result
  }
}
