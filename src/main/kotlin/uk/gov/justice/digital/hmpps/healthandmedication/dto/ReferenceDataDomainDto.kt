package uk.gov.justice.digital.hmpps.healthandmedication.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Reference Data Domain")
@JsonInclude(NON_NULL)
data class ReferenceDataDomainDto(
  @Schema(description = "Short code for the reference data domain", example = "HAIR")
  val code: String,

  @Schema(description = "Description of the reference data domain", example = "Hair type or colour")
  val description: String,

  @Schema(
    description = "The sequence number of the reference data domain. " +
      "Used for ordering domains correctly in lists. " +
      "0 is default order by description.",
    example = "3",
  )
  val listSequence: Int,

  @Schema(
    description = "Indicates that the reference data domain is active and can be used. " +
      "Inactive reference data domains are not returned by default in the API",
    example = "true",
  )
  val isActive: Boolean,

  @Schema(
    description = "The date and time the reference data domain was created",
  )
  val createdAt: ZonedDateTime,

  @Schema(
    description = "The username of the user who created the reference data domain",
    example = "USER1234",
  )
  val createdBy: String,

  @Schema(
    description = "The date and time the reference data domain was last modified",
  )
  val lastModifiedAt: ZonedDateTime?,

  @Schema(
    description = "The username of the user who last modified the reference data domain",
    example = "USER1234",
  )
  val lastModifiedBy: String?,

  @Schema(
    description = "The date and time the reference data domain was deactivated",
  )
  val deactivatedAt: ZonedDateTime?,

  @Schema(
    description = "The username of the user who deactivated the reference data domain",
    example = "USER1234",
  )
  val deactivatedBy: String?,

  @Schema(
    description = "The reference data codes associated with this reference data domain",
  )
  val referenceDataCodes: Collection<ReferenceDataCodeDto>,

  @Schema(
    description = "Reference data domains that are considered sub-domains of this domain",
  )
  val subDomains: Collection<ReferenceDataDomainDto>,
)
