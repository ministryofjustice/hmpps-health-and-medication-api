package uk.gov.justice.digital.hmpps.healthandmedication.utils

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReferenceCodeUtilsTest {

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @Test
  fun `should return null when id is null`() {
    val result = toReferenceDataCode(referenceDataCodeRepository, null)
    assertThat(result).isNull()
  }

  @Test
  fun `should return ReferenceDataCode when id is not null and found in repository`() {
    val id = ID
    val expectedReferenceDataCode = REFERENCE_DATA_CODE

    whenever(referenceDataCodeRepository.findById(id))
      .thenReturn(Optional.of(expectedReferenceDataCode))

    val result = toReferenceDataCode(referenceDataCodeRepository, id)
    assertThat(expectedReferenceDataCode).isEqualTo(result)
  }

  @Test
  fun `should throw IllegalArgumentException when id is not null and not found in repository`() {
    val id = "123"

    whenever(referenceDataCodeRepository.findById(id))
      .thenReturn(Optional.empty())

    assertThatThrownBy { toReferenceDataCode(referenceDataCodeRepository, id) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Invalid reference data code: $id")
  }

  private companion object {
    const val ID = "DOMAIN_CODE"
    val DOMAIN = ReferenceDataDomain("DOMAIN", "Domain", 0, ZonedDateTime.now(), "testUser")
    val REFERENCE_DATA_CODE =
      ReferenceDataCode(ID, "CODE", DOMAIN, "Ref data", 0, ZonedDateTime.now(), "testUser")
  }
}
