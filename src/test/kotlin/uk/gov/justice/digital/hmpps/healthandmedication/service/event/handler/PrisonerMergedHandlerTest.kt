package uk.gov.justice.digital.hmpps.healthandmedication.service.event.handler

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.opentelemetry.api.trace.Span
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.ReferenceDataValue
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.DietAndAllergyResponse
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationResponse
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.ReferenceDataSelection
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerHealthService
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.HmppsAdditionalInformation
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.HmppsDomainEvent
import java.time.ZonedDateTime

class PrisonerMergedHandlerTest {

  @Test
  fun `raises telemetry event when merged prisoner has diet and allergy data`() {
    val prisonerHealthService = mockk<PrisonerHealthService>()
    val currentSpan = mockk<Span>(relaxed = true)
    mockkStatic(Span::class)
    every { Span.current() } returns currentSpan
    every { prisonerHealthService.getHealth("B1234BB") } returns HealthAndMedicationResponse(
      dietAndAllergy = DietAndAllergyResponse(
        foodAllergies = ValueWithMetadata(
          listOf(
            ReferenceDataSelection(
              ReferenceDataValue("FOOD_ALLERGY_PEANUTS", "PEANUTS", "Peanuts"),
              "Sensitive",
            ),
          ),
          ZonedDateTime.now(),
          "USER1",
          "MDI",
        ),
      ),
    )

    PrisonerMergedHandler(prisonerHealthService).handle(
      HmppsDomainEvent(
        eventType = "prison-offender-events.prisoner.merged",
        additionalInformation = HmppsAdditionalInformation(mutableMapOf("nomsNumber" to "A1234AA", "removedNomsNumber" to "B1234BB")),
      ),
    )

    verify { currentSpan.addEvent(PrisonerMergedHandler.DIET_AND_ALLERGY_MERGE_INCOMPLETE, any<io.opentelemetry.api.common.Attributes>()) }
  }
}
