package org.zardina.repository

import org.zardina.Week

import scala.concurrent.Future

trait WeekRepository {

  def createGame(homeTeam: String, awayTeam: String, weekNumber: Int): Future[Int]
  def getGames(week: String): Future[Seq[Week]]

}
