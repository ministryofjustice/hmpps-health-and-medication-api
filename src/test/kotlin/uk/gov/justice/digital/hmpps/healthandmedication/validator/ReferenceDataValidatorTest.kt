package uk.gov.justice.digital.hmpps.healthandmedication.validator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness.LENIENT
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.ReferenceDataValidation
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class ReferenceDataValidatorTest {

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @InjectMocks
  lateinit var validator: ReferenceDataValidator

  @BeforeEach
  fun setUp() {
    whenever(referenceDataCodeRepository.findAllByDomainAndIncludeInactive("EXAMPLE_DOMAIN", false))
      .thenReturn(ACTIVE_CODES)

    validator.initialize(ReferenceDataValidation(arrayOf("EXAMPLE_DOMAIN")))
  }

  @Test
  fun `valid values`() {
    assertThat(validator.isValid(null, null)).isTrue()
    assertThat(validator.isValid(REFERENCE_DATA_CODE_ID_1, null)).isTrue()
    assertThat(validator.isValid(REFERENCE_DATA_CODE_ID_2, null)).isTrue()
  }

  @Test
  fun `invalid values`() {
    assertThat(validator.isValid("", null)).isFalse()
    assertThat(validator.isValid("   ", null)).isFalse()
    assertThat(validator.isValid("INVALID", null)).isFalse()
  }

  private companion object {
    val REFERENCE_DATA_DOMAIN = "DOMAIN1"
    val REFERENCE_DATA_CODE_ID_1 = "ID1"
    val REFERENCE_DATA_CODE_ID_2 = "ID2"
    val ACTIVE_CODES = listOf(REFERENCE_DATA_CODE_ID_1, REFERENCE_DATA_CODE_ID_2).map {
      ReferenceDataCode(
        id = it,
        domain = ReferenceDataDomain(
          code = REFERENCE_DATA_DOMAIN,
          description = "",
          listSequence = 0,
          createdBy = "",
        ),
        code = "CODE",
        description = "",
        listSequence = 0,
        createdBy = "",
      )
    }
  }
}
