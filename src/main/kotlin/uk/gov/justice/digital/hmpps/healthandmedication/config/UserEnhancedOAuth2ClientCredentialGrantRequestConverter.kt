package uk.gov.justice.digital.hmpps.personintegrationapi.config

import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter
import org.springframework.util.MultiValueMap
import java.util.Objects

@Suppress("UNCHECKED_CAST")
class UserEnhancedOAuth2ClientCredentialGrantRequestConverter : OAuth2ClientCredentialsGrantRequestEntityConverter() {

  fun enhanceWithUsername(
    grantRequest: OAuth2ClientCredentialsGrantRequest?,
    username: String?,
  ): RequestEntity<Any> {
    val request = super.convert(grantRequest)
    val headers = request.headers
    val body = Objects.requireNonNull(request).body
    val formParameters = body as MultiValueMap<String, Any>
    if (username != null) {
      formParameters.add("username", username)
    }
    return RequestEntity(formParameters, headers, HttpMethod.POST, request.url)
  }
}
