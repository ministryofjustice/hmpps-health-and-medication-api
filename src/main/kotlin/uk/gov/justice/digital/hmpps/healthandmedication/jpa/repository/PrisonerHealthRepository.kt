package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth

@Repository
interface PrisonerHealthRepository : JpaRepository<PrisonerHealth, String> {
  @Query(
    """
    SELECT h FROM PrisonerHealth h
    WHERE h.prisonerNumber IN :prisonerNumbers
    AND h.deletedAt IS NULL
    AND (
        EXISTS (
            SELECT 1 from FoodAllergy fa WHERE fa.prisonerNumber = h.prisonerNumber
        )
        OR EXISTS (
            SELECT 1 from MedicalDietaryRequirement mdr WHERE mdr.prisonerNumber = h.prisonerNumber
        )
        OR EXISTS (
            SELECT 1 from PersonalisedDietaryRequirement pdr WHERE pdr.prisonerNumber = h.prisonerNumber
        )
        OR EXISTS (
            SELECT 1 from CateringInstructions ci WHERE ci.prisonerNumber = h.prisonerNumber
        )
    )
    """,
  )
  fun findAllPrisonersWithDietaryNeeds(
    prisonerNumbers: MutableCollection<String>,
  ): List<PrisonerHealth>

  @Query(
    """
    SELECT h FROM PrisonerHealth h
    WHERE h.location IS NULL
    AND h.deletedAt IS NULL
    AND (
        EXISTS (
            SELECT 1 from FoodAllergy fa WHERE fa.prisonerNumber = h.prisonerNumber
        )
        OR EXISTS (
            SELECT 1 from MedicalDietaryRequirement mdr WHERE mdr.prisonerNumber = h.prisonerNumber
        )
        OR EXISTS (
            SELECT 1 from PersonalisedDietaryRequirement pdr WHERE pdr.prisonerNumber = h.prisonerNumber
        )
        OR EXISTS (
            SELECT 1 from CateringInstructions ci WHERE ci.prisonerNumber = h.prisonerNumber
        )
    )
    """,
  )
  fun findAllPrisonersWithoutLocationData(): List<PrisonerHealth>

  @Query(
    """
    SELECT h FROM PrisonerHealth h
    WHERE h.prisonerNumber = :prisonerNumber
    AND h.deletedAt IS NULL
    """,
  )
  fun findByPrisonerNumberAndDeletedAtIsNull(prisonerNumber: String): PrisonerHealth?
}
