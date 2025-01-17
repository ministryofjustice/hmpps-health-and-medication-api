package uk.gov.justice.digital.hmpps.healthandmedication.utils

import uk.gov.justice.digital.hmpps.healthandmedication.client.prisonersearch.PrisonerSearchClient

fun validatePrisonerNumber(prisonerSearchClient: PrisonerSearchClient, prisonerNumber: String) = require(prisonerSearchClient.getPrisoner(prisonerNumber)?.prisonerNumber == prisonerNumber) { "Prisoner number '$prisonerNumber' not found" }
