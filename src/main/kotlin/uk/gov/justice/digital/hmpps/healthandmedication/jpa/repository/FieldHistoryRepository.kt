package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldHistory
import java.time.ZonedDateTime
import java.util.SortedSet

@Repository
interface FieldHistoryRepository : JpaRepository<FieldHistory, Long> {
  fun findAllByPrisonerNumber(prisonerNumber: String): SortedSet<FieldHistory>

  fun findAllByPrisonerNumberAndField(prisonerNumber: String, field: HealthAndMedicationField): SortedSet<FieldHistory>

  fun findAllByPrisonerNumberAndCreatedAtBetweenOrderByFieldHistoryIdDesc(prisonerNumber: String, fromDate: ZonedDateTime, toDate: ZonedDateTime): SortedSet<FieldHistory>

  fun findFirstByPrisonerNumberAndFieldAndCreatedAtBeforeOrderByCreatedAtDesc(
    prisonerNumber: String,
    field: HealthAndMedicationField,
    createdAt: ZonedDateTime,
  ): FieldHistory?
}
