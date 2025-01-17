package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils

import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.healthandmedication.enums.HealthAndMedicationField
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.FieldHistory
import uk.gov.justice.digital.hmpps.healthandmedication.jpa.HistoryItem
import java.time.ZonedDateTime

data class HistoryComparison<T>(
  val value: T?,
  val createdAt: ZonedDateTime,
  val createdBy: String,
)

fun expectNoFieldHistoryFor(field: HealthAndMedicationField, history: Collection<FieldHistory>) {
  val fieldHistory = history.filter { it.field == field }.toList()
  assertThat(fieldHistory).isEmpty()
}

fun <T> assertHistoryItemEqual(
  expected: HistoryComparison<T>,
  actual: HistoryItem,
) {
  assertThat(actual.createdAt).isEqualTo(expected.createdAt)
  assertThat(actual.createdBy).isEqualTo(expected.createdBy)
}

// Refactor this for history items so that history item tests can all use it regardless of the fields
fun <T> expectFieldHistory(
  field: HealthAndMedicationField,
  history: Collection<FieldHistory>,
  vararg comparison: HistoryComparison<T>,
) {
  val fieldHistory = history.filter { it.field == field }.toList()
  assertThat(fieldHistory).hasSize(comparison.size)
  fieldHistory.forEachIndexed { index, actual ->
    val expected = comparison[index]
    assertThat(actual.field).isEqualTo(field)
    assertThat(field.get(actual)).isEqualTo(expected.value)
    assertHistoryItemEqual(expected, actual)
  }
}
