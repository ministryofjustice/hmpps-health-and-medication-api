package uk.gov.justice.digital.hmpps.healthandmedication.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.ReferenceDataDomainDto
import uk.gov.justice.digital.hmpps.healthandmedication.service.ReferenceDataDomainService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Reference Data Domains", description = "Reference Data Domains for Health and Medication data")
@RequestMapping("/reference-data/domains", produces = [MediaType.APPLICATION_JSON_VALUE])
class ReferenceDataDomainResource(
  private val referenceDataDomainService: ReferenceDataDomainService,
) {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO')")
  @Operation(
    summary = "Get all reference data domains",
    description = "Returns the list of reference data domains. " +
      "By default this endpoint only returns active reference data domains. " +
      "The `includeInactive` parameter can be used to return all reference data domains. " +
      "Requires role `ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Reference data domains found",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ReferenceDataDomainDto::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getReferenceDataDomains(
    @Parameter(
      description = "Include inactive reference data domains. Defaults to false.",
    )
    includeInactive: Boolean = false,
    @Parameter(
      description = "Include sub-domains at the top level. Defaults to false",
    )
    includeSubDomains: Boolean = false,
  ): Collection<ReferenceDataDomainDto> = referenceDataDomainService.getReferenceDataDomains(includeInactive, includeSubDomains)

  @GetMapping("/{domain}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO')")
  @Operation(
    summary = "Get a reference data domain",
    description = "Returns the reference data domain, including all reference data codes linked to that domain. " +
      "Requires role `ROLE_HEALTH_AND_MEDICATION_API__REFERENCE_DATA__RO`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Reference data domain retrieved",
        content = [Content(schema = Schema(implementation = ReferenceDataDomainDto::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Not found, the reference data domain was not found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getReferenceDataDomain(
    @PathVariable domain: String,
  ): ReferenceDataDomainDto = referenceDataDomainService.getReferenceDataDomain(domain)
}
