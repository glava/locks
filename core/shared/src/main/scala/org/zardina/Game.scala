package org.zardina

case class Game(id: String, home: String, away: String, week: Int)

case class Lock(userId: String, gameId: String, lockedTeam: String, points: Double)
