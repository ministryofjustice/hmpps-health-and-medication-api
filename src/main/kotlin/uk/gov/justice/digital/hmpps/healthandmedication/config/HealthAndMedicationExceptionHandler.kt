package uk.gov.justice.digital.hmpps.healthandmedication.config

import jakarta.servlet.ServletException
import jakarta.validation.ValidationException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.LOCKED
import org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NOT_IMPLEMENTED
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestControllerAdvice
class HealthAndMedicationExceptionHandler {

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.FORBIDDEN)
    .body(
      ErrorResponse(
        status = HttpStatus.FORBIDDEN.value(),
        userMessage = "Authentication problem. Check token and roles - ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Access denied exception: {}", e.message) }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Missing servlet request parameter exception: {}", e.message) }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
    val type = e.requiredType
    val message = if (type.isEnum) {
      "Parameter ${e.name} must be one of the following ${StringUtils.join(type.enumConstants, ", ")}"
    } else {
      "Parameter ${e.name} must be of type ${type.typeName}"
    }

    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: $message",
          developerMessage = e.message,
        ),
      ).also { log.info("Method argument type mismatch exception: {}", e.message) }
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: Couldn't read request body",
        developerMessage = e.message,
      ),
    ).also { log.info("HTTP message not readable exception: {}", e.message) }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure(s): ${
          e.allErrors.map { it.defaultMessage }.distinct().sorted().joinToString("\n")
        }",
        developerMessage = e.message,
      ),
    ).also { log.info("Validation exception: {}", e.message) }

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Illegal argument exception: {}", e.message) }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Validation exception: {}", e.message) }

  @ExceptionHandler(HandlerMethodValidationException::class)
  fun handleHandlerMethodValidationException(e: HandlerMethodValidationException): ResponseEntity<ErrorResponse> = e.allErrors.map { it.toString() }.distinct().sorted().joinToString("\n").let { validationErrors ->
    ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure(s): ${
            e.allErrors.map { it.defaultMessage }.distinct().sorted().joinToString("\n")
          }",
          developerMessage = "${e.message} $validationErrors",
        ),
      ).also { log.info("Validation exception: $validationErrors\n {}", e.message) }
  }

  @ExceptionHandler(HealthAndMedicationDataNotFoundException::class)
  fun handlePrisonPersonDataNotFoundException(e: HealthAndMedicationDataNotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "Health and medication data not found: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Health and medication data not found exception: {}", e.message) }

  @ExceptionHandler(ReferenceDataDomainNotFoundException::class)
  fun handleReferenceDataDomainNotFoundException(e: ReferenceDataDomainNotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "Reference data domain not found: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Reference data domain not found exception: {}", e.message) }

  @ExceptionHandler(ReferenceDataCodeNotFoundException::class)
  fun handleReferenceDataCodeNotFoundException(e: ReferenceDataCodeNotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "Reference data code not found: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Reference data code not found exception: {}", e.message) }

  @ExceptionHandler(IllegalFieldHistoryException::class)
  fun handleIllegalFieldHistoryException(e: IllegalFieldHistoryException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        userMessage = "Illegal field history: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Illegal field history exception: {}", e.message) }

  @ExceptionHandler(GenericNotFoundException::class)
  fun handleGenericNotFoundException(e: GenericNotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = e.message,
        developerMessage = e.message,
      ),
    )

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "No resource found failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("No resource found exception: {}", e.message) }

  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(METHOD_NOT_ALLOWED)
    .body(
      ErrorResponse(
        status = METHOD_NOT_ALLOWED,
        userMessage = "Method not allowed failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Method not allowed exception: {}", e.message) }

  @ExceptionHandler(ServletException::class)
  fun handleNotImplementedError(e: ServletException): ResponseEntity<ErrorResponse> = if (e.rootCause is NotImplementedError) {
    ResponseEntity
      .status(NOT_IMPLEMENTED)
      .body(
        ErrorResponse(
          status = NOT_IMPLEMENTED,
          userMessage = "Not implemented: ${e.message}",
          developerMessage = e.message,
        ),
      ).also { log.info("Not implemented: {}", e.message) }
  } else {
    handleUnexpectedException(e)
  }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> = handleUnexpectedException(e)

  fun handleUnexpectedException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        userMessage = "Unexpected error: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Unexpected exception", e) }

  @ExceptionHandler(DownstreamServiceException::class)
  fun handleDownstreamServiceException(e: DownstreamServiceException): ResponseEntity<ErrorResponse> {
    val cause = e.cause
    return if (cause is WebClientResponseException && cause.statusCode == LOCKED) {
      ResponseEntity
        .status(LOCKED)
        .body(
          ErrorResponse(
            status = LOCKED,
            userMessage = "Resource locked: ${e.message}",
            developerMessage = e.message,
          ),
        )
    } else {
      handleUnexpectedException(e)
    }
  }

  @ExceptionHandler(MissingServletRequestPartException::class)
  fun handleMissingServletRequestPartException(e: MissingServletRequestPartException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Required request part is missing: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Missing request part exception: {}", e.message) }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

class HealthAndMedicationDataNotFoundException(prisonerNumber: String) : Exception("No data for '$prisonerNumber'")
class ReferenceDataDomainNotFoundException(code: String) : Exception("No data for domain '$code'")
class ReferenceDataCodeNotFoundException(code: String, domain: String) : Exception("No data for code '$code' in domain '$domain'")

class GenericNotFoundException(message: String) : Exception(message)
class IllegalFieldHistoryException(prisonerNumber: String) : Exception("Cannot update field history for prisoner: '$prisonerNumber'")

class DownstreamServiceException(message: String, cause: Throwable) : Exception(message, cause)
