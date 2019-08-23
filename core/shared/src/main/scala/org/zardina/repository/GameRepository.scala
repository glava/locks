package org.zardina.repository

import org.zardina.Game

import scala.concurrent.Future

trait GameRepository {

  def createGame(homeTeam: String, awayTeam: String, weekNumber: Int): Future[Int]
  def getGames(week: String): Future[Seq[Game]]

}
