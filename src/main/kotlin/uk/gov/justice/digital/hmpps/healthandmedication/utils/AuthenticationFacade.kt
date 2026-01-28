package uk.gov.justice.digital.hmpps.healthandmedication.utils

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.healthandmedication.SYSTEM_USERNAME

@Component
class AuthenticationFacade {

  fun getUserOrSystemInContext() = currentUsername ?: SYSTEM_USERNAME

  val authentication: Authentication?
    get() = SecurityContextHolder.getContext().authentication

  val currentUsername: String?
    get() {
      val username: String?
      val userPrincipal = userPrincipal
      username = when (userPrincipal) {
        is String -> userPrincipal
        is UserDetails -> userPrincipal.username
        is Map<*, *> -> userPrincipal["username"] as String?
        else -> null
      }
      return username
    }

  private val userPrincipal: Any?
    get() {
      val auth = authentication
      return auth?.principal
    }
}
