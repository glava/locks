package org.zardina

import org.zardina.generator.StaticGamesLoader
import org.zardina.repository.{ LockRepository, TeamRepository, UserRepository, GameRepository }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ApiContextImplementation extends SangriaSchema.ApiContext with StaticGamesLoader {

  val lockRepository: LockRepository
  val teamRepository: TeamRepository
  val userRepository: UserRepository
  val gameRepository: GameRepository

  def addUser(nick: String, email: String, password: String): Future[User] = {
    userRepository.createUser(nick, email, password)
  }

  def getUser(email: String): Future[Option[User]] = {
    userRepository.getUser(email)
  }

  def games(week: Int): Future[Seq[Game]] = {
    gameRepository.getGames(week.toString)
  }

  def loadGames: Future[List[Int]] = {
    Future.sequence(staticGameDetails.map { g =>
      gameRepository.createGame(g.homeTeamAbbr, g.visitorTeamAbbr, g.week)
    })
  }
}
