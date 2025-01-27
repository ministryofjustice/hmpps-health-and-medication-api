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
class FoodAllergy(
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(name = "allergy", referencedColumnName = "id")
  var allergy: ReferenceDataCode,

  var commentText: String? = null,
) {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  val id: Long = 0

  fun toHistoryObject() = FoodAllergyHistoryItem(allergy.id, commentText)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as FoodAllergy

    if (prisonerNumber != other.prisonerNumber) return false
    if (allergy != other.allergy) return false
    if (commentText != other.commentText) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + allergy.hashCode()
    result = 31 * result + commentText.hashCode()
    return result
  }
}
