package org.zardina.repository

import scala.concurrent.Future

trait UserRepository {

  def createUser(nick: String, email: String): Future[Int]

}
