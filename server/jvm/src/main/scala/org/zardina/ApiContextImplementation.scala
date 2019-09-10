package org.zardina

import org.zardina.generator.StaticGamesLoader
import org.zardina.repository.{ AllTeams, GameRepository, LockRepository, TeamRepository, UserRepository }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ApiContextImplementation extends SangriaSchema.ApiContext with StaticGamesLoader {

  val dao: LockRepository with GameRepository with UserRepository with TeamRepository

  def addUser(nick: String, email: String, password: String): Future[User] = {
    dao.createUser(nick, email, password)
  }

  def user(email: String): Future[Option[User]] = {
    dao.getUser(email)
  }

  def games(week: Int): Future[Seq[Game]] = {
    dao.getGames(week)
  }

  def team(acronym: String): Future[Team] = {
    dao.team(acronym)
  }

  override def locks(userId: String, week: Int): Future[Seq[Lock]] = {
    dao.locks(userId, week)
  }

  def loadGames = {
    Future.sequence(staticGameDetails.map { g =>
      dao.createGame(g.homeTeamAbbr, g.visitorTeamAbbr, g.week)
    })
  }

  def createLock(gameId: String, homeTeamSelected: Boolean, userId: String): Future[Lock] = {
    for {
      game <- dao.getGame(gameId)
      lock <- dao.createLock(userId, gameId, if (homeTeamSelected) { game.home } else { game.away }, 1)
    } yield lock
  }
}
