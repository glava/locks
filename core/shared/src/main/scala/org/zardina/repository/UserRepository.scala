package org.zardina.repository

import org.zardina.User

import scala.concurrent.Future

trait UserRepository {

  def createUser(nick: String, email: String, password: String): Future[User]

  def getUser(id: String): Future[Option[User]]

  def getUsers(): Future[Seq[User]]

}
