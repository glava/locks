package org.zardina.repository

import scala.concurrent.Future

trait LockRepository {

  def createLock(userId: String, gameId: String, winningTeamId: String, points: Double): Future[org.zardina.Lock]

}
