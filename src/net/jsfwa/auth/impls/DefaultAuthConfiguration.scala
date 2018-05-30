package net.jsfwa.auth.impls

import javax.inject.Inject

import net.jsfwa.auth.{AuthConfiguration, CookieSessionContainer, TokenHandler}
import play.api.Configuration
import play.api.libs.json.{JsPath, Writes}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result, Results}

/**
  * Created by Andrei Zubrilin, 2018
  */
/**
  * Default auth configuration container
  *
  */

/**
  * Default Auth Types
  */
/**
  * Main auth config
  *
  * @param config
  */
class DefaultAuthConfiguration @Inject()(config: Configuration, tokenHandler: TokenHandler) extends AuthConfiguration {

  type Id = Int

  type AuthUser = String

  type Authority = String

  override val cookieSessionContainer: CookieSessionContainer[Id] = new DefaultCookieSessionContainer[Id](config, tokenHandler)(convertToId)

  /**
    * Default route configuration
    */
  override val loginRoute: String = config.getString("auth.login.route").getOrElse("/login")
  override val logoutRoute: String = config.getString("auth.logout.route").getOrElse("/logout")
  override val loginSuccessRoute: String = config.getString("auth.login.success.route").getOrElse("/dashboard")

  def convertToId(str: String) : Id = str.toInt

  /**
    * User authority confirmation
    *
    * @param userAuthority
    * @param authorities
    * @return
    */
  def validateAuthority(userAuthority: Authority, authorities: List[Authority]): Boolean = {
    authorities match {
      case Nil => true
      case _ => authorities.contains(userAuthority)
    }
  }


  /**
    * [Example] Get authority by user id
    *
    * @param userId
    * @return
    */
  def getAuthority(userId: Id): Authority = "Admin"

  /**
    * Action after successful authentication
    *
    * @return
    */
  def gotoLoginSuccess: Result = Redirect(loginSuccessRoute)


  /**
    * Action on failed authentication
    *
    * @return
    */
  def authenticationFailed: Result = gotoLogin

  /**
    * Login action
    *
    * @return
    */
  def gotoLogin: Result = Redirect(loginRoute)


  /**
    * Logout action
    *
    * @param request
    * @tparam A
    * @return
    */
  def logout[A](implicit request: Request[A]): Result = {
    cookieSessionContainer.invalidateSession(request)
    gotoLogin
  }

  /**
    * Action on authorization fail
    *
    * @return
    */
  def authorizationFailed: Result = gotoLogin

  /**
    * Authenticate valid user
    *
    * @param userId
    * @return
    */
  override def authenticate(userId: Id): Result = {
    cookieSessionContainer.startNewSession(userId)
    Results.Ok("success").withCookies(cookieSessionContainer.startNewSession(0))
  }
}