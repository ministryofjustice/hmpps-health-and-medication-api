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
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.NullishReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish.Defined
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish.Undefined
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class NullishReferenceDataCodeValidatorTest {

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @InjectMocks
  lateinit var validator: NullishReferenceDataCodeValidator

  @BeforeEach
  fun setUp() {
    whenever(referenceDataCodeRepository.findById("EXAMPLE_DOMAIN_CODE")).thenReturn(
      Optional.of(
        ReferenceDataCode(
          id = "EXAMPLE_DOMAIN_CODE",
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
        ),
      ),
    )

    whenever(referenceDataCodeRepository.findById("EXAMPLE_TWO_CODE")).thenReturn(
      Optional.of(
        ReferenceDataCode(
          id = "EXAMPLE_TWO_CODE",
          domain = ReferenceDataDomain(
            code = "OTHER_DOMAIN",
            description = "",
            listSequence = 0,
            createdBy = "",
          ),
          code = "CODE",
          description = "",
          listSequence = 0,
          createdBy = "",
        ),
      ),
    )
  }

  @Test
  fun `valid values (allowNull=true)`() {
    validator.initialize(NullishReferenceDataCode(domains = arrayOf("EXAMPLE_DOMAIN"), allowNull = true))
    assertThat(validator.isValid(Undefined, null)).isTrue()
    assertThat(validator.isValid(Defined("EXAMPLE_DOMAIN_CODE"), null)).isTrue()
    assertThat(validator.isValid(Defined(null), null)).isTrue()
  }

  @Test
  fun `invalid values (allowNull=true)`() {
    validator.initialize(NullishReferenceDataCode(domains = arrayOf("EXAMPLE_DOMAIN"), allowNull = true))
    assertThat(validator.isValid(Defined("NON_EXISTING_CODE"), null)).isFalse()
    assertThat(validator.isValid(Defined("EXAMPLE_TWO_CODE"), null)).isFalse()
  }

  @Test
  fun `valid values (allowNull=false)`() {
    validator.initialize(NullishReferenceDataCode(domains = arrayOf("EXAMPLE_DOMAIN"), allowNull = false))
    assertThat(validator.isValid(Undefined, null)).isTrue()
    assertThat(validator.isValid(Defined("EXAMPLE_DOMAIN_CODE"), null)).isTrue()
  }

  @Test
  fun `invalid values (allowNull=false)`() {
    validator.initialize(NullishReferenceDataCode(domains = arrayOf("EXAMPLE_DOMAIN"), allowNull = true))
    assertThat(validator.isValid(Defined("NON_EXISTING_CODE"), null)).isFalse()
    assertThat(validator.isValid(Defined("EXAMPLE_TWO_CODE"), null)).isFalse()
    assertThat(validator.isValid(Defined(null), null)).isTrue()
  }
}
