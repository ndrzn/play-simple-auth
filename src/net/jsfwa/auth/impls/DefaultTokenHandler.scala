package net.jsfwa.auth.impls

import java.security.SecureRandom
import javax.inject.Inject

import net.jsfwa.auth.{AuthenticityToken, TokenHandler}
import play.api.cache.AsyncCacheApi

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Random

/**
  * Created by Andrei Zubrilin, 2018
  */
/**
  * Just simple cache implementation, only for session tokens
  */
class DefaultTokenHandler @Inject()(cacheApi: AsyncCacheApi)(implicit ec: ExecutionContext) extends TokenHandler {

  implicit def keyToString[A](v: A): String = v.toString

  def put[A](key: A, token: AuthenticityToken): Unit = {
    cacheApi.get[List[AuthenticityToken]](key).map {
      case Some(tokens) => token :: tokens
      case _ => List(token)
    } foreach {
      tokens => cacheApi.set(key, tokens)
    }
  }

  def invalidateToken[A](key: A): Unit = cacheApi.remove(key)

  def invalidateToken[A](key: A, token: AuthenticityToken): Unit = {
    cacheApi.get[List[AuthenticityToken]](key).map {
      case Some(tokens) => cacheApi.set(key, tokens.filter(_ != token))
      case _ =>
    }
  }

  def exists[A](key: A, token: AuthenticityToken): Boolean = {
    Await.result(cacheApi.get[List[AuthenticityToken]](key).map(_.exists(_.contains(token))), 5.seconds)
  }

  def generateToken(tokenLength: Int = 64): AuthenticityToken = {
    val table = "abcdefghijklmnopqrstuvwxyz1234567890_.~*'()"
    val random = new Random(new SecureRandom())
    val token = Iterator.continually(random.nextInt(table.length)).map(table).take(tokenLength).mkString
    token.asInstanceOf[AuthenticityToken]
  }

}
