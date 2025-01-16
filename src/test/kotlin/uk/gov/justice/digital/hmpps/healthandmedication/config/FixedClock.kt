package uk.gov.justice.digital.hmpps.healthandmedication.config

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAmount

class FixedClock(var instant: Instant, private var zone: ZoneId) : Clock() {

  fun elapse(amountToAdd: TemporalAmount) {
    instant += amountToAdd
  }

  override fun instant(): Instant = this.instant
  override fun withZone(zone: ZoneId): Clock = this.apply { this.zone = zone }
  override fun getZone(): ZoneId = this.zone
}
