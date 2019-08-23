package org.zardina.repository

trait LockRepository {

  def createLock(userId: String, weekId: String, winningTeamId: String, points: Double)

}
