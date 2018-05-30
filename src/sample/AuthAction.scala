package sample

import javax.inject.Inject

import net.jsfwa.auth.{AuthConfiguration, BasicAuthElement, TokenPair}
import net.jsfwa.auth.impls.DefaultAuthConfiguration
import play.api.Configuration
import play.api.mvc.{BodyParsers, Request, Result}

import scala.concurrent.ExecutionContext

/**
  * Created by Andrei Zubrilin, 2018
  */

/**
  * Normal auth action only authenticated users allowed with proper authority
  *
  * @param config
  * @param parser
  * @param authConfig
  * @param ec
  */
class AuthAction @Inject()(config: Configuration, parser: BodyParsers.Default, override val authConfig: DefaultAuthConfiguration, ec: ExecutionContext) extends BasicAuthElement(config, parser, authConfig, ec) {

  import authConfig._

  def loggedId[A](implicit request: Request[A]): Id = request.attrs.get(userId).get
}

/**
  * Non-strict auth action, both guests and authorized users allowed
  *
  * @param config
  * @param parser
  * @param authConfig
  * @param ec
  */
class AuthActionOpt @Inject()(config: Configuration, parser: BodyParsers.Default, override val authConfig: DefaultAuthConfiguration, ec: ExecutionContext) extends BasicAuthElement(config, parser, authConfig, ec) {

  import authConfig._

  def loggedId[A, T](implicit request: Request[A]): Option[Id] = request.attrs.get(userId)

  override def authorize[A](authorities: Seq[Authority])(implicit request: Request[A]): Either[Result, Option[TokenPair[Id]]] = {
    cookieSessionContainer.validateCookies match {
      case Some(tokenPair) if validateAuthority(getAuthority(tokenPair.id), authorities.toList) =>
        Right(Some(tokenPair))
      case _ => Right(None)
    }
  }
}
