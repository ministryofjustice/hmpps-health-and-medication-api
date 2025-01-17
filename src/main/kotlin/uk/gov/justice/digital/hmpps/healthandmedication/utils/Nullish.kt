package uk.gov.justice.digital.hmpps.healthandmedication.utils

import kotlin.reflect.KMutableProperty0

/**
 * Nullish sealed interface to define a type that can have a **Defined** `value` including `null`, or be **Undefined**
 */
sealed interface Nullish<T> {
  data object Undefined : Nullish<Nothing>
  data class Defined<T>(val value: T?) : Nullish<T>

  /**
   * `apply` the value of the Nullish object to the nullable property `prop`
   *
   * If the Nullish object is **Undefined**, do nothing
   *
   * Otherwise, set the **Defined** `value` of the Nullish on the `prop` even if the `value` is `null`
   *
   * @param prop the nullable property to set to the **Defined** `value`
   */
  fun apply(prop: KMutableProperty0<T?>) {
    when (this) {
      Undefined -> return
      is Defined -> prop.set(value)
    }
  }

  /**
   * `apply` the value of the Nullish object to the nullable property `prop` after mapping via `fn`
   *
   * If the Nullish object is **Undefined**, do nothing
   *
   * Otherwise, map the **Defined** `value` of the Nullish using `fn` and set the mapped value
   * on the `prop` even if the `value` is `null`
   *
   * @param prop the nullable property to set to a mapped version of the **Defined** `value`
   * @param fn a function to map the `value` before setting on the `prop`
   */
  fun <U> apply(prop: KMutableProperty0<U?>, fn: (T) -> U?) {
    when (this) {
      is Undefined -> return
      is Defined -> {
        val currentValue = value
        val modifiedValue = currentValue?.let { fn(it) }
        prop.set(modifiedValue)
      }
    }
  }

  /**
   * If the Nullish object is defined, run the provided function
   * @param fn The function to run on the `value` if defined
   */
  fun <U> let(fn: (T?) -> Unit) {
    when (this) {
      is Undefined -> return
      is Defined -> {
        fn(value)
      }
    }
  }
}

/**
 * Get an attribute from the map of `attributes` and push it into a `Nullish` object
 *
 * @param map the map of attributes
 * @param name the name of the attribute to get
 */
inline fun <reified T> getAttributeAsNullish(
  map: Map<String, Any?>,
  name: String,
): Nullish<T> {
  if (!map.containsKey(name)) {
    @Suppress("UNCHECKED_CAST")
    return Nullish.Undefined as Nullish<T>
  }

  val value = map[name]
  if (value is T || value == null) {
    return Nullish.Defined(value as? T)
  } else {
    throw IllegalArgumentException("$name is not an instance of ${T::class.java.simpleName}")
  }
}
