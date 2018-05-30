package net.jsfwa

/**
  * Created by Andrei Zubrilin, 2018
  */
package object auth {

  type AuthenticityToken = String

  case class TokenPair[A](id: A, token: AuthenticityToken)
}
