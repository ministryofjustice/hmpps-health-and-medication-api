package uk.gov.justice.digital.hmpps.healthandmedication

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

const val SYSTEM_USERNAME = "HEALTH_AND_MEDICATION_API"

@SpringBootApplication
class HealthAndMedicationApi

fun main(args: Array<String>) {
  runApplication<HealthAndMedicationApi>(*args)
}
