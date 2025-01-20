package uk.gov.justice.digital.hmpps.healthandmedication.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class ReferenceDataDomainMapperTest {

  private val testDomain = ReferenceDataDomain("TEST_DOMAIN", "Domain description", 1, ZonedDateTime.now(), "testUser")
  private val testCode = ReferenceDataCode(
    id = "${testDomain}_TEST_CODE",
    domain = testDomain,
    code = "TEST_CODE",
    description = "Code description",
    listSequence = 1,
    createdAt = ZonedDateTime.now(),
    createdBy = "testUser",
  )

  @Test
  fun `toDto`() {
    val referenceDataDomain = testDomain
    testDomain.referenceDataCodes = mutableListOf(testCode)

    val dto = referenceDataDomain.toDto()

    assertThat(dto.code).isEqualTo("TEST_DOMAIN")
    assertThat(dto.description).isEqualTo("Domain description")
    assertThat(dto.referenceDataCodes.size).isEqualTo(1)
    assertThat(dto.referenceDataCodes.first().code).isEqualTo("TEST_CODE")
    assertThat(dto.referenceDataCodes.first().description).isEqualTo("Code description")
  }

  @Test
  fun `isActive when deactivatedAt is null`() {
    val referenceDataDomain = testCode

    assertThat(referenceDataDomain.isActive()).isEqualTo(true)
  }

  @Test
  fun `isActive when deactivatedAt is in the future`() {
    val referenceDataDomain = testCode
    referenceDataDomain.lastModifiedAt = ZonedDateTime.now()
    referenceDataDomain.lastModifiedBy = "testUser"
    referenceDataDomain.deactivatedAt = ZonedDateTime.now().plusDays(1)
    referenceDataDomain.deactivatedBy = "testUser"

    assertThat(referenceDataDomain.isActive()).isEqualTo(true)
  }

  @Test
  fun `isActive when deactivatedAt is in the past`() {
    val referenceDataDomain = testCode
    referenceDataDomain.lastModifiedAt = ZonedDateTime.now()
    referenceDataDomain.lastModifiedBy = "testUser"
    referenceDataDomain.deactivatedAt = ZonedDateTime.now().minusDays(1)
    referenceDataDomain.deactivatedBy = "testUser"

    assertThat(referenceDataDomain.isActive()).isEqualTo(false)
  }
}
