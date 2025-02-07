package uk.gov.justice.digital.hmpps.healthandmedication.client.prisonapi.request

import io.swagger.v3.oas.annotations.media.Schema

enum class PrisonApiSmokerStatus {
  Y,
  N,
  V,
}

@Schema(description = "Update to prisoner's smoker status")
data class PrisonApiSmokerStatusUpdate(
  @Schema(
    description = "The smoker status code ('Y' for 'Yes', 'N' for 'No', 'V' for 'Vaper/NRT Only')",
    example = "Y",
    allowableValues = ["Y", "N", "V"],
    required = true,
    nullable = true,
  )
  val smokerStatus: PrisonApiSmokerStatus?,
)
