package auth

import akka.stream.Materializer
import net.jsfwa.auth.impls.{DefaultAuthConfiguration, DefaultCookieSessionContainer, DefaultTokenHandler}
import net.jsfwa.auth.{TokenHandler}
import org.junit.Before
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.components.OneAppPerSuiteWithComponents
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api._
import play.api.cache.AsyncCacheApi
import play.api.cache.ehcache.EhCacheApi
import play.api.inject.Injector
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, _}
import sample.{AuthAction, AuthActionOpt}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Andrei Zubrilin, 2018
  */

class AuthSpec extends PlaySpec with BeforeAndAfterAll with GuiceOneAppPerSuite {

  implicit lazy val materializer: Materializer = app.materializer
  implicit val tokenHandler: TokenHandler = new DefaultTokenHandler(app.injector.instanceOf[AsyncCacheApi])(app.injector.instanceOf[ExecutionContext])
  implicit val defaultAuthConfiguration: DefaultAuthConfiguration = new DefaultAuthConfiguration(configuration, tokenHandler)
  lazy val configuration: Configuration = app.configuration
  val Authorized : AuthAction =  new AuthAction(configuration, app.injector.instanceOf[BodyParsers.Default], defaultAuthConfiguration, app.injector.instanceOf[ExecutionContext])
  val AuthorizedOpt : AuthActionOpt = new AuthActionOpt(configuration, app.injector.instanceOf[BodyParsers.Default], defaultAuthConfiguration, app.injector.instanceOf[ExecutionContext])


  /**
    * Tags:
    * No Auth - guest or user without proper authority
    * No User - just guest
    * Async - future body result
    * Opt - auth action with optional guests
    *
    */

  "1.[No Auth]TestRoute" should {
    "redirect to login without authentication" in {
      val action: EssentialAction = Authorized() {
        implicit request =>
          Results.Ok("")
      }

      val request = FakeRequest(POST, "/")

      val result = call(action, request)
      status(result) mustBe SEE_OTHER
      Helpers.headers(result) must contain("Location" -> "/login")
    }
  }

  "2.[With Auth]TestRoute" should {
    "proceed successfully" in {
      val action: EssentialAction = Authorized() {
        implicit request =>
          Results.Ok(Json.toJson(Authorized.loggedId.toString))
      }
      val request = FakeRequest(POST, "/").withCookies(defaultAuthConfiguration.cookieSessionContainer.startNewSession(0))

      val result = call(action, request)
      status(result) mustBe OK
      Helpers.contentAsString(result) must include("0")
    }
  }

  "3.[No Auth; Opt]TestRoute" should {
    "redirect to login without authentication" in {
      val action: EssentialAction = AuthorizedOpt("User") {
        implicit request =>
          Results.Ok("")
      }

      val request = FakeRequest(POST, "/").withCookies(defaultAuthConfiguration.cookieSessionContainer.startNewSession(0))

      val result = call(action, request)
      status(result) mustBe SEE_OTHER
      Helpers.headers(result) must contain("Location" -> "/login")
    }
  }

  "4.[With Auth; Opt]TestRoute" should {
    "proceed successfully" in {
      val action: EssentialAction = AuthorizedOpt() {
        implicit request =>
          Results.Ok(AuthorizedOpt.loggedId.toString)
      }

      val request = FakeRequest(POST, "/").withCookies(defaultAuthConfiguration.cookieSessionContainer.startNewSession(0))

      val result = call(action, request)
      status(result) mustBe OK
      Helpers.contentAsString(result) must include("0")
    }
  }

  "5.[With Auth; Opt; No User]TestRoute" should {
    "proceed successfully" in {
      val action: EssentialAction = AuthorizedOpt() {
        implicit request =>
          Results.Ok(Json.toJson(AuthorizedOpt.loggedId.toString))
      }

      val request = FakeRequest(POST, "/")

      val result = call(action, request)
      status(result) mustBe OK
      Helpers.contentAsString(result) must include("None")
    }
  }

  "6.[Async; No Auth]TestRoute" should {
    "redirect to login without authentication" in {
      val action: EssentialAction = Authorized.async() {
        implicit request =>
          Future.successful(Results.Ok(""))
      }

      val request = FakeRequest(POST, "/")

      val result = call(action, request)
      status(result) mustBe SEE_OTHER
      Helpers.headers(result) must contain("Location" -> "/login")
    }
  }

  "7.[Async; No Auth; Opt]TestRoute" should {
    "redirect to login without authentication" in {
      val action: EssentialAction = AuthorizedOpt.async("User") {
        implicit request =>
          Future.successful(Results.Ok("!"))
      }

      val request = FakeRequest(POST, "/").withCookies(defaultAuthConfiguration.cookieSessionContainer.startNewSession(0))

      val result = call(action, request)
      status(result) mustBe SEE_OTHER
      Helpers.headers(result) must contain("Location" -> "/login")
    }
  }

}
