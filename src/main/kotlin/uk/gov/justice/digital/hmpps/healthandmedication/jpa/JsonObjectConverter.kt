package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component

@Converter
@Component
class JsonObjectConverter(val objectMapper: ObjectMapper) : AttributeConverter<JsonObject, String> {
  override fun convertToDatabaseColumn(jsonObject: JsonObject?): String? = jsonObject?.let(objectMapper::writeValueAsString)

  override fun convertToEntityAttribute(json: String?): JsonObject? = json?.let { objectMapper.readValue(it, JsonObject::class.java) }
}
