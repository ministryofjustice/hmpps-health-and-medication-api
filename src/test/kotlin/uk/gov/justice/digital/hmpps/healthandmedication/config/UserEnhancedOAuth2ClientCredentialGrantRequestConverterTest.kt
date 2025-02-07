package uk.gov.justice.digital.hmpps.healthandmedication.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.util.LinkedMultiValueMap
import uk.gov.justice.digital.hmpps.personintegrationapi.config.UserEnhancedOAuth2ClientCredentialGrantRequestConverter

class UserEnhancedOAuth2ClientCredentialGrantRequestConverterTest {

  private lateinit var oAuth2ClientCredentialsGrantRequest: OAuth2ClientCredentialsGrantRequest

  private val underTest: UserEnhancedOAuth2ClientCredentialGrantRequestConverter =
    UserEnhancedOAuth2ClientCredentialGrantRequestConverter()

  @BeforeEach
  fun setUp() {
    oAuth2ClientCredentialsGrantRequest = OAuth2ClientCredentialsGrantRequest(CLIENT_REGISTRATION)
  }

  @Test
  fun `client credentials grant request has the username added`() {
    val response = underTest.enhanceWithUsername(oAuth2ClientCredentialsGrantRequest, TEST_USERNAME)
    assertThat((response.body as LinkedMultiValueMap<*, *>)["username"]).isEqualTo(listOf(TEST_USERNAME))
  }

  companion object {
    const val TEST_USERNAME = "TEST_USERNAME"
    val CLIENT_REGISTRATION: ClientRegistration =
      ClientRegistration
        .withRegistrationId("test_id")
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .tokenUri("oauth/token")
        .scope("read")
        .userNameAttributeName("id")
        .clientName("Client Name")
        .clientId("client-id")
        .clientSecret("client-secret").build()
  }
}
