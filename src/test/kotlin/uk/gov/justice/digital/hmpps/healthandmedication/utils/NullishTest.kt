package uk.gov.justice.digital.hmpps.healthandmedication.utils

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class NullishTest {
  @Test
  fun `should return Defined when attribute is present and of correct type`() {
    val attributes = mapOf("key1" to "value1")
    val result = getAttributeAsNullish<String>(attributes, "key1")
    assertThat(Nullish.Defined("value1")).isEqualTo(result)
  }

  @Test
  fun `should return Defined with null when attribute is present and value is null`() {
    val attributes = mapOf("key1" to null)
    val result = getAttributeAsNullish<String>(attributes, "key1")
    assertThat(Nullish.Defined<String>(null)).isEqualTo(result)
  }

  @Test
  fun `should return Undefined when attribute is not present in the map`() {
    val attributes = mapOf("key1" to "value1")
    val result = getAttributeAsNullish<String>(attributes, "key2")
    assertThat(Nullish.Undefined).isEqualTo(result)
  }

  @Test
  fun `should throw IllegalArgumentException when attribute is present but of wrong type`() {
    val attributes = mapOf("key1" to 123)
    assertThatThrownBy { getAttributeAsNullish<String>(attributes, "key1") }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("key1 is not an instance of String")
  }

  @Test
  fun `should not run function provided when attribute is not present in the map`() {
    val attributes = mapOf("key1" to 123)
    var check = "unchanged"
    getAttributeAsNullish<String>(attributes, "key2").let<String> { check = "changed" }

    assertThat(check).isEqualTo("unchanged")
  }

  @Test
  fun `should run function provided when attribute is present in the map`() {
    val attributes = mapOf("key1" to "changed")
    var check: String? = "unchanged"
    getAttributeAsNullish<String>(attributes, "key1").let<String> { check = it }

    assertThat(check).isEqualTo("changed")
  }
}
