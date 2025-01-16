package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain

@Repository
interface ReferenceDataDomainRepository : JpaRepository<ReferenceDataDomain, String> {

  @Query(
    """
        SELECT rdd
        FROM ReferenceDataDomain rdd 
        WHERE (
              :includeInactive = true OR 
              (:includeInactive = false AND rdd.deactivatedAt IS NULL)
              ) AND (
                (:includeSubDomains = true) OR 
                (:includeSubDomains = false AND rdd.parentDomainCode IS NULL)
              )
        ORDER BY CASE 
                    WHEN rdd.listSequence = 0 THEN 999
                    ELSE rdd.listSequence
                 END,
                 rdd.description
    """,
  )
  fun findAllByIncludeInactive(
    @Param("includeInactive") includeInactive: Boolean,
    @Param("includeSubDomains") includeSubDomains: Boolean = false,
  ): Collection<ReferenceDataDomain>

  fun findByCode(code: String): ReferenceDataDomain?
}
