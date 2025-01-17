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
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.NullishReferenceDataCodeList
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish.Defined
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish.Undefined

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class NullishReferenceDataCodeListValidatorTest {

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @InjectMocks
  lateinit var validator: NullishReferenceDataCodeListValidator

  @BeforeEach
  fun setUp() {
    whenever(referenceDataCodeRepository.findAllByDomainAndIncludeInactive("EXAMPLE_DOMAIN", false)).thenReturn(
      ACTIVE_CODES,
    )

    validator.initialize(NullishReferenceDataCodeList(arrayOf("EXAMPLE_DOMAIN")))
  }

  @Test
  fun `valid values`() {
    assertThat(validator.isValid(Undefined, null)).isTrue()
    assertThat(validator.isValid(Defined(listOf("EXAMPLE_DOMAIN_CODE")), null)).isTrue()
    assertThat(validator.isValid(Defined(listOf("EXAMPLE_DOMAIN_TWO")), null)).isTrue()
    assertThat(validator.isValid(Defined(listOf("EXAMPLE_DOMAIN_CODE", "EXAMPLE_DOMAIN_TWO")), null)).isTrue()
  }

  @Test
  fun `invalid values`() {
    assertThat(validator.isValid(Defined(null), null)).isFalse()
    assertThat(validator.isValid(Defined(listOf("EXAMPLE_DOMAIN_FAKE")), null)).isFalse()
    assertThat(validator.isValid(Defined(listOf("EXAMPLE_DOMAIN_CODE", "EXAMPLE_DOMAIN_FAKE")), null)).isFalse()
  }

  private companion object {
    val ACTIVE_CODES = listOf("EXAMPLE_DOMAIN_CODE", "EXAMPLE_DOMAIN_TWO").map {
      ReferenceDataCode(
        id = it,
        domain = ReferenceDataDomain(
          code = "EXAMPLE_DOMAIN",
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
