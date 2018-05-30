package sample

import javax.inject.Inject

import net.jsfwa.auth._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Andrei Zubrilin, 2018
  */

/**
  * Dont forget to provide implicitly your own implementation of AuthContainer to AuthAction
  *
  * @param cc
  * @param ec
  */
class ExampleController @Inject()(Authorized: AuthAction, AuthorizedOpt: AuthActionOpt, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def testRoute()= Authorized.async(){ implicit request =>
    Future.successful(Ok("Ok"))
  }

  def testRouteOpt()= AuthorizedOpt.async(){ implicit request =>
    Future.successful(Ok("Ok"))
  }
}
