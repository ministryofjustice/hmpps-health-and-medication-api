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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.healthandmedication.config.HealthAndMedicationDataNotFoundException
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.UpdateDietAndAllergyRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.UpdateSmokerStatusRequest
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.DietAndAllergyResponse
import uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerHealthService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Health and Medication Data")
@RequestMapping("/prisoners/{prisonerNumber}", produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthAndMedicationResource(private val prisonerHealthService: PrisonerHealthService) {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO', 'ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW')")
  @Operation(
    description = "Requires role `ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO` or `ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns Health and Medication Data",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Data not found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getHealthAndMedicationData(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
  ) = prisonerHealthService.getHealth(prisonerNumber)
    ?: throw HealthAndMedicationDataNotFoundException(prisonerNumber)

  @PutMapping("/diet-and-allergy")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW')")
  @Operation(
    summary = "Updates the diet and allergy data for a prisoner",
    description = "Requires role `ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the updated diet and allergy data",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun updateDietAndAllergyData(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable
    prisonerNumber: String,
    @RequestBody
    @Valid
    updateDietAndAllergyRequest: UpdateDietAndAllergyRequest,
  ): DietAndAllergyResponse = prisonerHealthService.updateDietAndAllergyData(prisonerNumber, updateDietAndAllergyRequest)

  @PutMapping("/smoker")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW')")
  @Operation(
    summary = "Updates the smoker status for a prisoner",
    description = "Requires role `ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "No content",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun updateSmokerStatus(
    @Schema(description = "The prisoner number", example = "A1234AA", required = true)
    @PathVariable prisonerNumber: String,
    @RequestBody(required = true) @Valid request: UpdateSmokerStatusRequest,
  ) {
    prisonerHealthService.updateSmokerStatus(prisonerNumber, request)
  }
}
