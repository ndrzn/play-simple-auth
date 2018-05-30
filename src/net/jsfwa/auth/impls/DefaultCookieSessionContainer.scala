package net.jsfwa.auth.impls

import javax.inject.Inject

import net.jsfwa.auth._
import play.api.Configuration
import play.api.mvc._

import scala.util.Try

/**
  * Created by Andrei Zubrilin, 2018
  */
/**
  * Default cookie session control
  *
  * @param tokenHandler
  */
class DefaultCookieSessionContainer[Id] (config: Configuration, tokenHandler: TokenHandler)(implicit toId: String => Id) extends CookieSessionContainer[Id] {

  override val cookiesName: String = config.getString("auth.cookies.name").getOrElse("PLAY_AUTH")
  override val cookiesMaxAge: Int = config.getInt("auth.cookies.max.age").getOrElse(10000)
  override val cookieHttpOnlyOption: Boolean = config.getBoolean("auth.cookies.httponly").getOrElse(true)

  def startNewSession(userId: Id): Cookie = {
    val token = tokenHandler.generateToken()
    tokenHandler.put(userId, token)
    defaultCookie(TokenPair(userId, token))
  }

  def extractTokenPair[A](implicit request: Request[A]): Option[TokenPair[Id]] = {
    request.cookies.get(cookiesName).flatMap { s =>
      s.value.split(":") match {
        case l if l.length == 2 => Try {
          TokenPair(toId(l(0)), l(1))
        }.toOption
        case _ => None
      }
    }
  }

  def validateCookies[A](implicit request: Request[A]): Option[TokenPair[Id]] = {
    extractTokenPair match {
      case Some(tp) if tokenHandler.exists(tp.id, tp.token) => Some(tp)
      case _ => None
    }
  }

  def defaultCookie(tokenPair: TokenPair[Id]): Cookie = {
    Cookie(name = cookiesName, value = s"${tokenPair.id}:" + tokenPair.token, maxAge = Some(cookiesMaxAge), httpOnly = cookieHttpOnlyOption)
  }

  def invalidateSession[A](implicit request: Request[A]): Unit = {
    extractTokenPair.foreach(t => tokenHandler.invalidateToken(t.id, t.token))
  }
}

