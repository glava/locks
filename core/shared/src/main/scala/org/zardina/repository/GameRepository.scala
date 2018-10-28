package org.zardina.repository

trait GameRepository {

  def createGame(userId: String, weekId: String, winningTeamId: String, points: Double)

}
