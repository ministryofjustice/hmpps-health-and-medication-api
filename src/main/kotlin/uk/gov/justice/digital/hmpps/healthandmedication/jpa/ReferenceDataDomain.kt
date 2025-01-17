package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.hibernate.Hibernate
import java.time.ZonedDateTime

@Entity
class ReferenceDataDomain(
  @Id
  @Column(name = "code", updatable = false, nullable = false)
  val code: String,

  val description: String,
  val listSequence: Int,
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val createdBy: String,
) {
  var lastModifiedAt: ZonedDateTime? = null
  var lastModifiedBy: String? = null
  var deactivatedAt: ZonedDateTime? = null
  var deactivatedBy: String? = null
  var parentDomainCode: String? = null

  @OneToMany(mappedBy = "domain")
  var referenceDataCodes: MutableList<ReferenceDataCode> = mutableListOf()

  @OneToMany(mappedBy = "parentDomainCode")
  var subDomains: MutableList<ReferenceDataDomain> = mutableListOf()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as ReferenceDataDomain

    if (code != other.code) return false
    if (description != other.description) return false
    if (listSequence != other.listSequence) return false

    return true
  }

  override fun hashCode(): Int {
    var result = code.hashCode()
    result = 31 * result + (description.hashCode())
    result = 31 * result + (listSequence)
    return result
  }

  override fun toString(): String = "ReferenceDataDomain(code=$code, description='$description', listSequence=$listSequence)"
}
