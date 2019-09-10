package org.zardina.repository

import org.zardina

import scala.concurrent.Future

trait LockRepository {

  def createLock(userId: String, gameId: String, winningTeamId: String, points: Double): Future[org.zardina.Lock]

  def locks(userId: String, week: Int): Future[Seq[zardina.Lock]]

}
