package uk.gov.justice.digital.hmpps.healthandmedication.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.healthandmedication.service.PrisonerLocationService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@Tag(name = "Admin only")
@RequestMapping("/migration", produces = [MediaType.APPLICATION_JSON_VALUE])
class MigrationResource(
  private val prisonerLocationService: PrisonerLocationService,
  @Value("\${migration.endpoint.enabled:false}") private val migrationEndpointEnabled: Boolean,
) {
  @PostMapping("/location")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW')")
  @Operation(
    description = "Should only be performed by a member of the development team. Requires role `ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Migration successful",
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
        responseCode = "501",
        description = "Endpoint is currently disabled",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun migrateLocationData(): ResponseEntity<Void> {
    if (migrationEndpointEnabled) {
      prisonerLocationService.migrateLocationData()
      return ResponseEntity.ok().build()
    } else {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }
  }
}
