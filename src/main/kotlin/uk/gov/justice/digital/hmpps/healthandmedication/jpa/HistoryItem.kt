package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import java.time.ZonedDateTime

interface HistoryItem : Comparable<HistoryItem> {
  val createdAt: ZonedDateTime
  val createdBy: String
  var prisonId: String
  var mergedAt: ZonedDateTime?
  var mergedFrom: String?

  override fun compareTo(other: HistoryItem) = compareValuesBy(
    this,
    other,
    { it.createdAt },
    { it.hashCode() },
  )
}
