package org.zardina

case class AuthenticationException(message: String) extends Exception(message)
case class AuthorizationException(message: String) extends Exception(message)