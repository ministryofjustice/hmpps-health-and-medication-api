package uk.gov.justice.digital.hmpps.healthandmedication.validator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.NullishRange
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish.Defined
import uk.gov.justice.digital.hmpps.healthandmedication.utils.Nullish.Undefined

class NullishRangeValidatorTest {

  private lateinit var validator: NullishRangeValidator

  @BeforeEach
  fun setUp() {
    validator = NullishRangeValidator()
    validator.initialize(NullishRange(min = 5, max = 10))
  }

  @Test
  fun `valid values`() {
    assertThat(validator.isValid(Undefined, null)).isTrue()
    assertThat(validator.isValid(Defined(null), null)).isTrue()
    assertThat(validator.isValid(Defined(5), null)).isTrue()
    assertThat(validator.isValid(Defined(10), null)).isTrue()
    assertThat(validator.isValid(Defined(7), null)).isTrue()
  }

  @Test
  fun `invalid values`() {
    assertThat(validator.isValid(Defined(4), null)).isFalse()
    assertThat(validator.isValid(Defined(11), null)).isFalse()
    assertThat(validator.isValid(Defined(-1), null)).isFalse()
    assertThat(validator.isValid(Defined(0), null)).isFalse()
  }
}
