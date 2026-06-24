package uk.gov.justice.digital.hmpps.healthandmedication.service.event

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField.FOOD_ALLERGY
import uk.gov.justice.digital.hmpps.healthandmedication.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.healthandmedication.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FoodAllergy
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.PrisonerHealth
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils.RepopulateDb
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.response.HealthAndMedicationResponse
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.DomainEventsListener.Companion.PRISONER_MERGED
import uk.gov.justice.digital.hmpps.healthandmedication.service.event.PersonReference.Companion.withPrisonNumber
import uk.gov.justice.digital.hmpps.healthandmedication.utils.toReferenceDataCode
import java.time.Duration.ofSeconds
import java.time.ZonedDateTime

class PrisonerMergedIntTest : IntegrationTestBase() {

  @Autowired
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  companion object {
    private const val PRISONER_NUMBER_NOT_FOUND = "Z9999ZZ"
    private const val PARENT_PRISONER_NUMBER = "A1234AA"
    private const val CHILD_PRISONER_NUMBER = "B1234BB"
    private const val USER1 = "USER1"
  }

  @Disabled("Merge handling not yet implemented")
  @Test
  fun `message ignored if prison number not of interest`() {
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER_NOT_FOUND))).isEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isEmpty()
    assertThat(fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isNull()

    sendDomainEvent(personMergedEvent(PRISONER_NUMBER_NOT_FOUND, PRISONER_NUMBER_NOT_FOUND))

    await withPollDelay ofSeconds(1) untilCallTo { hmppsDomainEventsQueue.countAllMessagesOnQueue() } matches { it == 0 }
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER_NOT_FOUND))).isEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isEmpty()
    assertThat(fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND)).isNull()
  }

  @Disabled("Merge handling not yet implemented")
  @Test
  @Transactional
  @RepopulateDb
  fun `merge event causes health data to be deleted for the old prisoner number`() {
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).isNotEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).isNotEmpty()
    assertThat(fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).isNotEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER)).isNotNull()
    sendDomainEvent(personMergedEvent(PRISONER_NUMBER, PRISONER_NUMBER))

    await withPollDelay ofSeconds(1) untilCallTo { hmppsDomainEventsQueue.countAllMessagesOnQueue() } matches { it == 0 }
    assertThat(prisonerHealthRepository.findAllPrisonersWithDietaryNeeds(mutableSetOf(PRISONER_NUMBER))).isEmpty()
    assertThat(fieldMetadataRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).isEmpty()
    assertThat(fieldHistoryRepository.findAllByPrisonerNumber(PRISONER_NUMBER)).isEmpty()
    assertThat(prisonerLocationRepository.findByPrisonerNumber(PRISONER_NUMBER)).isNull()
  }

  @Test
  @Transactional
  @RepopulateDb
  fun `manual merge should soft-delete child record`() {
    // set up two entities: a parent record and a linked child record
    val peanuts = toReferenceDataCode(referenceDataCodeRepository, "FOOD_ALLERGY_PEANUTS")!!

    val parent = PrisonerHealth(PARENT_PRISONER_NUMBER)
    prisonerHealthRepository.save(parent)

    val child = PrisonerHealth(CHILD_PRISONER_NUMBER).apply {
      pendingMergeToPrisonerNumber = PARENT_PRISONER_NUMBER
      foodAllergies = mutableSetOf(FoodAllergy(CHILD_PRISONER_NUMBER, peanuts))
    }
    prisonerHealthRepository.save(child)

    TestTransaction.flagForCommit()
    TestTransaction.end()

    // initiate merge (currently only deals with field history & metadata)
    webTestClient.put().uri("/prisoners/$PARENT_PRISONER_NUMBER/merge-completion")
      .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW")))
      .exchange()
      .expectStatus().isNoContent

    // child record is soft-deleted and 404s
    webTestClient.get().uri("/prisoners/$CHILD_PRISONER_NUMBER")
      .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO")))
      .exchange()
      .expectStatus().isNotFound

    TestTransaction.start()

    // soft-deleted child still exists with the same information (for manual restoration purposes)
    val softDeletedChild = prisonerHealthRepository.findById(CHILD_PRISONER_NUMBER).get()
    assertThat(softDeletedChild.pendingMergeToPrisonerNumber).isNull()
    assertThat(softDeletedChild.deletedAt).isEqualTo(ZonedDateTime.now(clock))
    assertThat(softDeletedChild.deletionReason).isEqualTo("Merged into $PARENT_PRISONER_NUMBER")
    assertThat(softDeletedChild.foodAllergies).contains(FoodAllergy(CHILD_PRISONER_NUMBER, peanuts))

    // parent's pending merge list is empty
    webTestClient.get().uri("/prisoners/$PARENT_PRISONER_NUMBER")
      .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<HealthAndMedicationResponse>()
      .returnResult().responseBody!!.pendingMerges.isEmpty()

    TestTransaction.end()
  }

  @Test
  @Transactional
  @RepopulateDb
  fun `manual merge should transfer field history from child record to parent record`() {
    // set up two entities: a parent record and a linked child record
    val peanuts = toReferenceDataCode(referenceDataCodeRepository, "FOOD_ALLERGY_PEANUTS")!!

    val parent = PrisonerHealth(PARENT_PRISONER_NUMBER)
    prisonerHealthRepository.save(parent)

    val child = PrisonerHealth(CHILD_PRISONER_NUMBER).apply {
      pendingMergeToPrisonerNumber = PARENT_PRISONER_NUMBER
      fieldHistory.add(
        FieldHistory(
          prisonerNumber = CHILD_PRISONER_NUMBER,
          field = FOOD_ALLERGY,
          valueRef = peanuts,
          createdBy = USER1,
          prisonId = "MDI",
          createdAt = ZonedDateTime.now(clock).minusDays(1),
        ),
      )
    }
    prisonerHealthRepository.save(child)

    // Commit changes so webTestClient can see them
    TestTransaction.flagForCommit()
    TestTransaction.end()

    // initiate merge (currently only deals with field history & metadata)
    webTestClient.put().uri("/prisoners/$PARENT_PRISONER_NUMBER/merge-completion")
      .headers(setAuthorisation(roles = listOf("ROLE_HEALTH_AND_MEDICATION_API__HEALTH_AND_MEDICATION_DATA__RW")))
      .exchange()
      .expectStatus().isNoContent

    // Restart transaction to check DB
    TestTransaction.start()
    // check that the child record's field history has been copied across to the parent record
    val parentHistory = fieldHistoryRepository.findAllByPrisonerNumber(PARENT_PRISONER_NUMBER)
    val transferredHistory = parentHistory.find { it.field == FOOD_ALLERGY && it.mergedFrom == CHILD_PRISONER_NUMBER }
    assertThat(transferredHistory).isNotNull()
    assertThat(transferredHistory?.prisonerNumber).isEqualTo(PARENT_PRISONER_NUMBER)
    assertThat(transferredHistory?.mergedAt).isEqualTo(ZonedDateTime.now(clock))
    assertThat(transferredHistory?.valueRef).isEqualTo(peanuts)
  }

  private fun personMergedEvent(
    prisonNumber: String,
    removedPrisonNumber: String,
    occurredAt: ZonedDateTime = ZonedDateTime.now(),
    eventType: String = PRISONER_MERGED,
    detailUrl: String? = null,
    description: String = "A prisoner was merged",
  ) = HmppsDomainEvent(
    eventType,
    1,
    detailUrl,
    occurredAt,
    description,
    HmppsAdditionalInformation(mutableMapOf("nomsNumber" to prisonNumber, "removedNomsNumber" to removedPrisonNumber)),
    withPrisonNumber(prisonNumber),
  )
}
