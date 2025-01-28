package uk.gov.justice.digital.hmpps.healthandmedication.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reference data code ID selection with comment")
class ReferenceDataIdSelection(
  @Schema(description = "Selected reference data code ID", required = true)
  val value: String,

  @Schema(
    description = "User supplied comment about this selection",
    example = "Some other text",
    required = false,
  )
  val comment: String? = null,
)
