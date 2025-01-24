package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.MedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth

@Repository
interface PrisonerHealthRepository : JpaRepository<PrisonerHealth, String> {
  fun findAllByPrisonerNumberIn(prisonerNumbers: MutableCollection<String>, pageable: Pageable): Page<PrisonerHealth>
  fun findAllByPrisonerNumberInAndFoodAllergiesIsNotEmptyOrMedicalDietaryRequirementsIsNotEmpty(
    prisonerNumbers: MutableCollection<String>,
    pageable: Pageable,
  ): Page<PrisonerHealth>

  fun findAllByPrisonerNumberInAndFoodAllergiesIsNotEmptyOrMedicalDietaryRequirementsIsNotEmpty(prisonerNumbers: MutableCollection<String>): List<PrisonerHealth>
}
