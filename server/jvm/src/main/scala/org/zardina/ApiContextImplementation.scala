package org.zardina

import org.zardina.generator.StaticGamesLoader
import org.zardina.repository.{ AllTeams, GameRepository, LockRepository, TeamRepository, UserRepository }

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

  def team(acronym: String): Future[Team] = {
    teamRepository.team(acronym)
  }

  def loadGames: Future[List[Int]] = {
    Future.sequence(staticGameDetails.map { g =>
      gameRepository.createGame(g.homeTeamAbbr, g.visitorTeamAbbr, g.week)
    } ++ AllTeams.teams.map { case (name, acronym) => teamRepository.createTeam(name, acronym) })
  }

  def createLock(week: Int, gameId: String, homeTeamSelected: Boolean, userId: String) = {
    Future.successful(1)
  }
}
