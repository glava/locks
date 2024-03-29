package org.zardina.repository

import org.zardina.Game

import scala.concurrent.Future

trait GameRepository {

  def createGame(homeTeam: String, awayTeam: String, weekNumber: Int, isoTime: Long, localTime: String): Future[org.zardina.Game]

  def getGames(week: Int): Future[Seq[Game]]

  def getGame(home: String, away: String, week: Int): Future[Game]

  def getGame(id: String): Future[Game]
}
