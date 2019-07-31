package org.zardina

import org.zardina.generator.StaticGamesLoader
import org.zardina.repository.{ GameRepository, TeamRepository, UserRepository, WeekRepository }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ApiContextImplementation extends SangriaSchema.ApiContext with StaticGamesLoader {

  val gameRepository: GameRepository
  val teamRepository: TeamRepository
  val userRepository: UserRepository
  val weekRepository: WeekRepository

  def addUser(nick: String, email: String, password: String): Future[User] = {
    userRepository.createUser(nick, email, password)
  }

  def getUser(email: String): Future[Option[User]] = {
    userRepository.getUser(email)
  }

  def getGames(week: Int): Future[Seq[Week]] = {
    weekRepository.getGames(week.toString)
  }

  def updateGames: Future[List[Int]] = {
    Future.sequence(staticGameDetails.map { g =>
      weekRepository.createGame(g.homeTeamAbbr, g.visitorTeamAbbr, g.week)
    })
  }
}
