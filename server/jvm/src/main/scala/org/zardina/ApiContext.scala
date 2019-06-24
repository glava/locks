package org.zardina

import org.zardina.repository.{ GameRepository, TeamRepository, UserRepository, WeekRepository }

import scala.concurrent.Future

trait ApiContext {

  val gameRepository: GameRepository
  val teamRepository: TeamRepository
  val userRepository: UserRepository
  val weekRepository: WeekRepository

  def addUser(nick: String, email: String, password: String): Future[User] = {
    userRepository.createUser(nick, email, password)
  }

  def getUser(email: String) = {
    userRepository.getUser(email)
  }

}
