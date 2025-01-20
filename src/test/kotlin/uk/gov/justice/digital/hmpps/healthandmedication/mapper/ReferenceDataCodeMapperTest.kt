package uk.gov.justice.digital.hmpps.healthandmedication.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import java.time.ZonedDateTime

class ReferenceDataCodeMapperTest {

  private val testDomain = ReferenceDataDomain("TEST", "Test domain", 1, ZonedDateTime.now(), "testUser")
  private val testCode = ReferenceDataCode(
    id = "${testDomain}_ORANGE",
    domain = testDomain,
    code = "ORANGE",
    description = "Orange",
    listSequence = 1,
    createdAt = ZonedDateTime.now(),
    createdBy = "testUser",
  )

  @Test
  fun `test toDto with default description`() {
    val referenceDataCode = ReferenceDataCode(
      id = "${testDomain}_ORANGE",
      domain = testDomain,
      code = "ORANGE",
      description = "Orange",
      listSequence = 1,
      createdAt = ZonedDateTime.now(),
      createdBy = "testUser",
    )

    val dto = referenceDataCode.toDto()

    assertThat(dto.domain).isEqualTo("TEST")
    assertThat(dto.code).isEqualTo("ORANGE")
    assertThat(dto.description).isEqualTo("Orange") // Should use default description
  }

  @Test
  fun `test isActive when deactivatedAt is null`() {
    val referenceDataCode = testCode

    assertThat(referenceDataCode.isActive()).isEqualTo(true)
  }

  @Test
  fun `test isActive when deactivatedAt is in the future`() {
    val referenceDataCode = testCode
    referenceDataCode.lastModifiedAt = ZonedDateTime.now()
    referenceDataCode.lastModifiedBy = "testUser"
    referenceDataCode.deactivatedAt = ZonedDateTime.now().plusDays(1)
    referenceDataCode.deactivatedBy = "testUser"

    assertThat(referenceDataCode.isActive()).isEqualTo(true)
  }

  @Test
  fun `test isActive when deactivatedAt is in the past`() {
    val referenceDataCode = testCode
    referenceDataCode.lastModifiedAt = ZonedDateTime.now()
    referenceDataCode.lastModifiedBy = "testUser"
    referenceDataCode.deactivatedAt = ZonedDateTime.now().minusDays(1)
    referenceDataCode.deactivatedBy = "testUser"

    assertThat(referenceDataCode.isActive()).isEqualTo(false)
  }
}
