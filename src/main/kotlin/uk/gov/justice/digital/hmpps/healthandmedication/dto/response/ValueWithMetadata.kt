package uk.gov.justice.digital.hmpps.healthandmedication.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

data class ValueWithMetadata<T>(
  @Schema(description = "Value")
  val value: T?,

  @Schema(description = "Timestamp this field was last modified")
  val lastModifiedAt: ZonedDateTime,

  @Schema(description = "Username of the user that last modified this field", example = "USER1")
  val lastModifiedBy: String,
)
