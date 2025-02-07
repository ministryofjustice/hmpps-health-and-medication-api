package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.ReferenceDataValidation

@Schema(description = "Update to prisoner's smoker status")
data class UpdateSmokerStatusRequest(
  @Schema(
    description = "Smoker status. Valid `ReferenceDataCode.id` options for `smokerStatus` can be retrieved " +
      "by querying `GET /reference-data/domains/SMOKER`.",
    type = "string",
    example = "SMOKER_NO",
    requiredMode = REQUIRED,
    nullable = true,
  )
  @field:ReferenceDataValidation(domains = ["SMOKER"])
  val smokerStatus: String?,
)
