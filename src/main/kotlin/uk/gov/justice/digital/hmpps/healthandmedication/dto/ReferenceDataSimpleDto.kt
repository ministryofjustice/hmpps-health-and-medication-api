package uk.gov.justice.digital.hmpps.healthandmedication.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reference Data Simple DTO - for use in dropdowns")
@JsonInclude(NON_NULL)
data class ReferenceDataSimpleDto(
  @Schema(description = "Id", example = "FOOD_ALLERGY_MILK")
  val id: String,

  @Schema(description = "Description of the reference data code", example = "Milk")
  val description: String,

  @Schema(
    description = "The sequence number of the reference data code. " +
      "Used for ordering reference data correctly in lists and dropdowns. " +
      "0 is default order by description.",
    example = "3",
  )
  val listSequence: Int,

  @Schema(
    description = "Indicates that the reference data code is active and can be used. " +
      "Inactive reference data codes are not returned by default in the API",
    example = "true",
  )
  val isActive: Boolean,
)
