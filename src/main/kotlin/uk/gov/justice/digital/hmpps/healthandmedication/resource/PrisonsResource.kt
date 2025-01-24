package uk.gov.justice.digital.hmpps.healthandmedication.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.healthandmedication.config.HealthAndMedicationDataNotFoundException
import uk.gov.justice.digital.hmpps.healthandmedication.resource.requests.HealthAndMedicationForPrisonRequest
import uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerHealthService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Bulk Health and Medication Data by Agency")
@RequestMapping("/prisons/{prisonId}", produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonsResource(private val prisonerHealthService: PrisonerHealthService){
  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO')")
  @Operation(
    description = "Requires role `ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns Health and Medication Data",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun postHealthAndMedicationData(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable prisonId: String,
    @Valid @RequestBody request:HealthAndMedicationForPrisonRequest
  ) = prisonerHealthService.getHealthForPrison(prisonId, request)
    ?: throw HealthAndMedicationDataNotFoundException(prisonId)
}
