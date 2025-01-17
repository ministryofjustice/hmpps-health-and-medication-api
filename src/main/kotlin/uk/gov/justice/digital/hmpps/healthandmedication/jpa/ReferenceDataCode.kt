package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.Hibernate
import java.time.ZonedDateTime

@Entity
class ReferenceDataCode(
  @Id
  @Column(name = "id", updatable = false, nullable = false)
  val id: String,

  @Column(name = "code", updatable = false, nullable = false)
  val code: String,

  @ManyToOne
  @JoinColumn(name = "domain", nullable = false)
  val domain: ReferenceDataDomain,

  val description: String,
  val listSequence: Int,
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
) {
  var lastModifiedAt: ZonedDateTime? = null
  var lastModifiedBy: String? = null
  var deactivatedAt: ZonedDateTime? = null
  var deactivatedBy: String? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as ReferenceDataCode

    if (id != other.id) return false
    if (code != other.code) return false
    if (domain != other.domain) return false
    if (description != other.description) return false
    if (listSequence != other.listSequence) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + (code.hashCode())
    result = 31 * result + (domain.hashCode())
    result = 31 * result + (description.hashCode())
    result = 31 * result + (listSequence)
    return result
  }

  override fun toString(): String = "ReferenceDataCode(id=$id, code=$code, domain=$domain, description='$description', listSequence=$listSequence)"
}
