package org.zardina.repository

import scala.concurrent.Future

trait WeekRepository {

  def createWeek(homeTeam: String, awayTeam: String, weekNumber: Int): Future[Int]

}
