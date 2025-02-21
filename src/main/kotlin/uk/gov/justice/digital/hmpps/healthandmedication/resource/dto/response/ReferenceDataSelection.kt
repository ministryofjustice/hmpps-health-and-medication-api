package uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.ReferenceDataValue

@Schema(description = "Reference data selection with comment")
data class ReferenceDataSelection(
  @Schema(description = "Selected reference data details", required = true)
  val value: ReferenceDataValue,

  @Schema(description = "User supplied comment about this selection", example = "Some other text", required = false)
  val comment: String? = null,
)
