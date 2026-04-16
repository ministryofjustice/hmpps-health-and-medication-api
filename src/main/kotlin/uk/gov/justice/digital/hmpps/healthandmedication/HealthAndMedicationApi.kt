package uk.gov.justice.digital.hmpps.healthandmedication

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

const val SYSTEM_USERNAME = "HEALTH_AND_MEDICATION_API"

@SpringBootApplication
@ConfigurationPropertiesScan
class HealthAndMedicationApi

fun main(args: Array<String>) {
  runApplication<HealthAndMedicationApi>(*args)
}
