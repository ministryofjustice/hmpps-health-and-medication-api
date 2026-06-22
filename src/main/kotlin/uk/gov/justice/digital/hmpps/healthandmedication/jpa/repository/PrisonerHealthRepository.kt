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
    AND h.pendingMergeToPrisonerNumber IS NULL
    AND (
      EXISTS (SELECT 1 FROM FoodAllergy fa WHERE fa.prisonerNumber = h.prisonerNumber)
      OR EXISTS (SELECT 1 FROM MedicalDietaryRequirement mdr WHERE mdr.prisonerNumber = h.prisonerNumber)
      OR EXISTS (SELECT 1 FROM PersonalisedDietaryRequirement pdr WHERE pdr.prisonerNumber = h.prisonerNumber)
      OR EXISTS (SELECT 1 FROM CateringInstructions ci WHERE ci.prisonerNumber = h.prisonerNumber)
      OR EXISTS (
        SELECT 1 FROM PrisonerHealth ph WHERE ph.pendingMergeToPrisonerNumber = h.prisonerNumber
        AND ph.deletedAt IS NULL
        AND (
          EXISTS (SELECT 1 FROM FoodAllergy fa WHERE fa.prisonerNumber = ph.prisonerNumber)
          OR EXISTS (SELECT 1 FROM MedicalDietaryRequirement mdr WHERE mdr.prisonerNumber = ph.prisonerNumber)
          OR EXISTS (SELECT 1 FROM PersonalisedDietaryRequirement pdr WHERE pdr.prisonerNumber = ph.prisonerNumber)
          OR EXISTS (SELECT 1 FROM CateringInstructions ci WHERE ci.prisonerNumber = ph.prisonerNumber)
        )
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
    AND h.pendingMergeToPrisonerNumber IS NULL
    AND (
      EXISTS (SELECT 1 FROM FoodAllergy fa WHERE fa.prisonerNumber = h.prisonerNumber)
      OR EXISTS (SELECT 1 FROM MedicalDietaryRequirement mdr WHERE mdr.prisonerNumber = h.prisonerNumber)
      OR EXISTS (SELECT 1 FROM PersonalisedDietaryRequirement pdr WHERE pdr.prisonerNumber = h.prisonerNumber)
      OR EXISTS (SELECT 1 FROM CateringInstructions ci WHERE ci.prisonerNumber = h.prisonerNumber)
      OR EXISTS (
        SELECT 1 FROM PrisonerHealth ph WHERE ph.pendingMergeToPrisonerNumber = h.prisonerNumber
        AND ph.deletedAt IS NULL
        AND (
          EXISTS (SELECT 1 FROM FoodAllergy fa WHERE fa.prisonerNumber = ph.prisonerNumber)
          OR EXISTS (SELECT 1 FROM MedicalDietaryRequirement mdr WHERE mdr.prisonerNumber = ph.prisonerNumber)
          OR EXISTS (SELECT 1 FROM PersonalisedDietaryRequirement pdr WHERE pdr.prisonerNumber = ph.prisonerNumber)
          OR EXISTS (SELECT 1 FROM CateringInstructions ci WHERE ci.prisonerNumber = ph.prisonerNumber)
        )
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

  @Query(
    """
    SELECT h FROM PrisonerHealth h
    WHERE h.prisonerNumber = :prisonerNumber
    AND h.deletedAt IS NOT NULL
    """,
  )
  fun findByPrisonerNumberAndDeletedAtIsNotNull(prisonerNumber: String): PrisonerHealth?
}
