package uk.gov.justice.digital.hmpps.healthandmedication.jpa

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Converter
@Component
class JsonObjectConverter(val jsonMapper: JsonMapper) : AttributeConverter<JsonObject, String> {
  override fun convertToDatabaseColumn(jsonObject: JsonObject?): String? = jsonObject?.let(jsonMapper::writeValueAsString)

  override fun convertToEntityAttribute(json: String?): JsonObject? = json?.let { jsonMapper.readValue(it, JsonObject::class.java) }
}
