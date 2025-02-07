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
import uk.gov.justice.digital.hmpps.healthandmedication.annotation.ReferenceDataListValidation
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataCode
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.ReferenceDataCodeRepository
import uk.gov.justice.digital.hmpps.healthandmedication.resource.dto.request.ReferenceDataIdSelection

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = LENIENT)
class ReferenceDataListValidatorTest {

  @Mock
  lateinit var referenceDataCodeRepository: ReferenceDataCodeRepository

  @InjectMocks
  lateinit var validator: ReferenceDataListValidator

  @BeforeEach
  fun setUp() {
    whenever(referenceDataCodeRepository.findAllByDomainAndIncludeInactive("EXAMPLE_DOMAIN", false))
      .thenReturn(ACTIVE_CODES)

    validator.initialize(ReferenceDataListValidation(arrayOf("EXAMPLE_DOMAIN")))
  }

  @Test
  fun `valid values`() {
    assertThat(validator.isValid(emptyList(), null)).isTrue()
    assertThat(validator.isValid(listOf(VALID_SELECTION_1), null)).isTrue()
    assertThat(validator.isValid((listOf(VALID_SELECTION_2)), null)).isTrue()
    assertThat(validator.isValid((listOf(VALID_SELECTION_1, VALID_SELECTION_2)), null)).isTrue()
  }

  @Test
  fun `invalid values`() {
    assertThat(validator.isValid(null, null)).isFalse()
    assertThat(validator.isValid(listOf(INVALID_SELECTION), null)).isFalse()
    assertThat(validator.isValid(listOf(VALID_SELECTION_1, INVALID_SELECTION), null)).isFalse()
  }

  private companion object {
    val REFERENCE_DATA_DOMAIN = "DOMAIN1"
    val REFERENCE_DATA_CODE_ID_1 = "ID1"
    val REFERENCE_DATA_CODE_ID_2 = "ID2"
    val VALID_SELECTION_1 = ReferenceDataIdSelection(REFERENCE_DATA_CODE_ID_1)
    val VALID_SELECTION_2 = ReferenceDataIdSelection(REFERENCE_DATA_CODE_ID_2)
    val INVALID_SELECTION = ReferenceDataIdSelection("INVALID")
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
