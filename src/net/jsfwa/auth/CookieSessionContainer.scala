package net.jsfwa.auth

import com.google.inject.ImplementedBy
import net.jsfwa.auth.impls.DefaultCookieSessionContainer
import play.api.mvc.{Cookie, Request}

/**
  * Created by Andrei Zubrilin, 2018
  */
/**
  * Used to describe actions with cookies
  */
@ImplementedBy(classOf[DefaultCookieSessionContainer[Int]])
trait CookieSessionContainer[Id]{

  val cookiesName: String = "PLAY_AUTH"
  val cookiesMaxAge: Int = 10000
  val cookieHttpOnlyOption: Boolean = true

  def startNewSession(userId: Id): Cookie

  /**
    * Validate cookies(user access) on each request
    *
    * @param request
    * @tparam A
    * @return
    */
  def validateCookies[A](implicit request: Request[A]) : Option[TokenPair[Id]]

  def defaultCookie(tokenPair: TokenPair[Id]) : Cookie

  def invalidateSession[A](implicit request: Request[A])
}




