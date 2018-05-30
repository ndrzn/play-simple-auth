package net.jsfwa.auth

import com.google.inject.ImplementedBy
import net.jsfwa.auth.impls.DefaultAuthConfiguration
import play.api.mvc.{Request, Result}

/**
  * Created by Andrei Zubrilin, 2018
  */
/**
  * Used to define basic auth behavior
  */
@ImplementedBy(classOf[DefaultAuthConfiguration])
trait AuthConfiguration {

  type Id

  type AuthUser

  type Authority

  val cookieSessionContainer: CookieSessionContainer[Id]

  val loginRoute: String

  val logoutRoute: String

  val loginSuccessRoute: String

  def gotoLogin: Result

  def convertToId(str : String) : Id

  def gotoLoginSuccess: Result

  def logout[A](implicit request: Request[A]): Result

  def getAuthority(userId: Id) : Authority

  def authenticationFailed: Result

  def authorizationFailed: Result

  def validateAuthority(userAuthority: Authority, authorities: List[Authority]): Boolean

  def authenticate(userId: Id) : Result
}
