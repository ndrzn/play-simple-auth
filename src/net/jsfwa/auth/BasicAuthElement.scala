package net.jsfwa.auth

import javax.inject.Inject

import net.jsfwa.auth.impls.DefaultAuthConfiguration
import play.api.Configuration
import play.api.libs.json.{Format, Json}
import play.api.libs.typedmap.TypedKey
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Andrei Zubrilin, 2018
  */

/**
  * Basic auth action functionality
  *
  */
abstract class BasicAuthElement (config: Configuration, parser: BodyParsers.Default, val authConfig: AuthConfiguration, ec: ExecutionContext) {
  self =>

  import authConfig._

  implicit def toAuthority[A](a: A): authConfig.Authority = a.asInstanceOf[Authority]

  implicit def toAuthority[A](a: Seq[A]): Seq[authConfig.Authority] = a.asInstanceOf[Seq[Authority]]

  implicit val userId: TypedKey[Id] = TypedKey.apply[Id]("authId")

  /**
    * Proxy auth action with different constructors
    * Encapsulates Play Framework default constructors
    */
  private object AuthProxy extends ActionBuilderImpl(parser)(ec) {
    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      block(request)
    }

    /**
      * Usual action with list of authorities
      *
      * @param authorities
      * @param block
      * @return
      */
    def apply(authorities: Seq[Authority])
             (block: Request[AnyContent] => Future[Result]): Action[AnyContent] = super.async {
      implicit request =>
        this.action(authorities)(block)

    }

    /**
      * Action with custom BodyParser
      *
      * @param bodyParser
      * @param authorities
      * @param block
      * @tparam A
      * @return
      */
    def apply[A](bodyParser: BodyParser[A], authorities: Seq[Authority])(block: Request[A] => Future[Result]): Action[A] = super.async(bodyParser) {
      implicit request =>
        this.action(authorities)(block)

    }

    /**
      * Just actual action method
      *
      * @param authorities
      * @param block
      * @param request
      * @tparam A
      * @return
      */
    private def action[A](authorities: Seq[Authority])(block: Request[A] => Future[Result])
                         (implicit request: Request[A]) = {
      self.authorize(authorities) match {
        case Left(r) => Future.successful(r)
        case Right(tp) if tp.isDefined => block(request.addAttr(userId, tp.get.id)).map(_.withCookies(cookieSessionContainer.defaultCookie(tp.get)))
        case _ => block(request)
      }
    }

  }

  /**
    * Normal action
    *
    * @param authorities
    * @param block
    * @return
    */
  def apply[A](authorities: A*)(block: Request[AnyContent] => Result): Action[AnyContent] = this.proxyCall(authorities)(block andThen Future.successful)

  /**
    * Normal action with BodyParser
    *
    * @param bodyParser
    * @param authorities
    * @param block
    * @tparam A
    * @return
    */
  def apply[A, B](bodyParser: BodyParser[B], authorities: A*)(block: Request[B] => Result): Action[B] = this.proxyCall(bodyParser, authorities)(block andThen Future.successful)

  /**
    * Async action
    *
    * @param authorities
    * @param block
    * @return
    */
  def async[A](authorities: A*)(block: Request[AnyContent] => Future[Result]): Action[AnyContent] = this.proxyCall(authorities)(block)

  /**
    * Async with Body Parser
    *
    * @param bodyParser
    * @param authorities
    * @param block
    * @tparam A
    * @return
    */
  def async[A, B](bodyParser: BodyParser[B], authorities: A*)(block: Request[B] => Future[Result]): Action[B] = this.proxyCall(bodyParser, authorities)(block)

  /**
    * Proceed to actual action calls
    *
    * @param authorities
    * @param block
    * @return
    */
  private def proxyCall(authorities: Seq[Authority])(block: Request[AnyContent] => Future[Result]) = AuthProxy(authorities)(block)

  /**
    * Proceed to actual action calls
    *
    * @param bodyParser
    * @param authorities
    * @param block
    * @tparam A
    * @return
    */
  private def proxyCall[A](bodyParser: BodyParser[A], authorities: Seq[Authority])(block: Request[A] => Future[Result]) = AuthProxy(bodyParser, authorities)(block)

  /**
    * Default authorization method
    *
    * @param authorities
    * @param request
    * @tparam A
    * @return
    */
  def authorize[A](authorities: Seq[Authority])(implicit request: Request[A]): Either[Result, Option[TokenPair[Id]]] = {
    cookieSessionContainer.validateCookies match {
      case Some(tokenPair) if validateAuthority(getAuthority(tokenPair.id), authorities.toList) =>
        Right(Some(tokenPair))
      case _ => Left(authorizationFailed)
    }
  }
}

