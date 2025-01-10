package uk.gov.justice.digital.hmpps.healthandmedicationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HealthAndMedicationApi

fun main(args: Array<String>) {
  runApplication<HealthAndMedicationApi>(*args)
}
