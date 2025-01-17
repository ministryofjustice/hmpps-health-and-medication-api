package uk.gov.justice.digital.hmpps.healthandmedication.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.healthandmedication.config.ReferenceDataCodeNotFoundException
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class ReferenceDataCodeServiceTest {

  @Mock
  private lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @InjectMocks
  private lateinit var referenceDataCodeService: ReferenceDataCodeService

  @Test
  fun `test getReferenceDataCodes with includeInactive`() {
    val activeCode = ACTIVE_CODE
    val inactiveCode = INACTIVE_CODE
    whenever(referenceDataCodeRepository.findAllByDomainAndIncludeInactive(DOMAIN.code, true)).thenReturn(
      listOf(
        activeCode,
        inactiveCode,
      ),
    )

    val result = referenceDataCodeService.getReferenceDataCodes(DOMAIN.code, true)

    assertThat(result.size).isEqualTo(2)
    verify(referenceDataCodeRepository).findAllByDomainAndIncludeInactive(DOMAIN.code, true)
  }

  @Test
  fun `test getReferenceDataCodes without includeInactive`() {
    val activeCode = ACTIVE_CODE
    whenever(referenceDataCodeRepository.findAllByDomainAndIncludeInactive(DOMAIN.code, false)).thenReturn(
      listOf(
        activeCode,
      ),
    )

    val result = referenceDataCodeService.getReferenceDataCodes(DOMAIN.code, false)

    assertThat(result.size).isEqualTo(1)
    verify(referenceDataCodeRepository).findAllByDomainAndIncludeInactive(DOMAIN.code, false)
  }

  @Test
  fun `test getReferenceDataCode found`() {
    val code = "ACTIVE"
    val referenceDataCode = ACTIVE_CODE

    whenever(referenceDataCodeRepository.findByCodeAndDomainCode(code, DOMAIN.code)).thenReturn(
      referenceDataCode,
    )

    val result = referenceDataCodeService.getReferenceDataCode(code, DOMAIN.code)

    assertNotNull(result)
    assertThat(result.code).isEqualTo(code)
    verify(referenceDataCodeRepository).findByCodeAndDomainCode(code, DOMAIN.code)
  }

  @Test
  fun `test getReferenceDataCode not found`() {
    val code = "NONEXISTENT"
    whenever(referenceDataCodeRepository.findByCodeAndDomainCode(code, DOMAIN.code)).thenReturn(null)

    val exception = assertThrows(ReferenceDataCodeNotFoundException::class.java) {
      referenceDataCodeService.getReferenceDataCode(code, DOMAIN.code)
    }

    assertThat(exception.message).isEqualTo("No data for code 'NONEXISTENT' in domain 'DOMAIN'")
    verify(referenceDataCodeRepository).findByCodeAndDomainCode(code, DOMAIN.code)
  }

  private companion object {
    val DOMAIN = ReferenceDataDomain("DOMAIN", "Domain", 0, ZonedDateTime.now(), "testUser")
    val ACTIVE_CODE =
      ReferenceDataCode("${DOMAIN.code}_ACTIVE", "ACTIVE", DOMAIN, "Active domain", 0, ZonedDateTime.now(), "testUser")
    val INACTIVE_CODE =
      ReferenceDataCode(
        "${DOMAIN.code}_INACTIVE",
        "INACTIVE",
        DOMAIN,
        "Inactive domain",
        0,
        ZonedDateTime.now(),
        "testUser",
      ).apply {
        this.deactivatedAt = ZonedDateTime.now()
        this.deactivatedBy = "testUser"
      }
  }
}
