package net.jsfwa.auth

import com.google.inject.ImplementedBy
import net.jsfwa.auth.impls.DefaultTokenHandler


/**
  * Created by Andrei Zubrilin, 2018
  */
/**
  * The control of a session storage
  */
@ImplementedBy(classOf[DefaultTokenHandler])
trait TokenHandler {

  def put[A](key: A, token: AuthenticityToken)

  def invalidateToken[A](key: A)

  def invalidateToken[A](key: A, token: AuthenticityToken)

  def generateToken(tokenLength : Int = 64): AuthenticityToken

  def exists[A](key: A, token: AuthenticityToken): Boolean
}
