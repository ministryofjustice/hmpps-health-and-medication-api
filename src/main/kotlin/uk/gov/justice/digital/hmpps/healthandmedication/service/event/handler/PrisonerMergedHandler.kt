package uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.DietAndAllergyResponse
import uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerHealthService
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.nomsNumber
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.removedNomsNumber

@Transactional
@Service
class PrisonerMergedHandler(private val prisonerHealthService: PrisonerHealthService) {
  fun handle(personMerged: HmppsDomainEvent) {
    val removedNomsNumber = personMerged.additionalInformation.removedNomsNumber
    val dietAndAllergyRecordForRemovedPrisonerRecord =
      prisonerHealthService.getHealth(removedNomsNumber)?.dietAndAllergy

    // For now we are just logging the fact that a merge has thrown away diet and allergy data for a prisoner
    // - work is outstanding to provide a mechanism to flag merges up to staff so they can review the merge.
    if (hasDietAndAllergyData(dietAndAllergyRecordForRemovedPrisonerRecord)) {
      Span.current().addEvent(
        DIET_AND_ALLERGY_MERGE_INCOMPLETE,
        Attributes.builder()
          .put("nomsNumber", personMerged.additionalInformation.nomsNumber)
          .put("removedNomsNumber", removedNomsNumber)
          .build(),
      )
    }
  }

  private fun hasDietAndAllergyData(dietAndAllergy: DietAndAllergyResponse?): Boolean = dietAndAllergy?.let {
    it.foodAllergies?.value?.isNotEmpty() == true ||
      it.medicalDietaryRequirements?.value?.isNotEmpty() == true ||
      it.personalisedDietaryRequirements?.value?.isNotEmpty() == true ||
      it.cateringInstructions?.value?.isNotBlank() == true
  } == true

  companion object {
    const val DIET_AND_ALLERGY_MERGE_INCOMPLETE = "diet-and-allergy-merge-incomplete"
  }
}
