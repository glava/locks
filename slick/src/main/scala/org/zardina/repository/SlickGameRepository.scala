package org.zardina.repository

import java.util.UUID

import javax.sql.DataSource
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

case class SlickGame(id: String, userId: String, weekId: String, lockedTeam: String, points: Double, created: Long, updated: Long)

class SlickGameRepository(dataSource: DataSource)(implicit val jdbcProfile: JdbcProfile, executionContext: ExecutionContext) extends GameRepository {

  import jdbcProfile.api._

  val db = jdbcProfile.backend.Database.forDataSource(dataSource, None, AsyncExecutor.default("locks-game-repository"))

  class Game(tag: Tag) extends Table[SlickGame](tag, "_GAME") {
    def id = column[String]("_ID", O.PrimaryKey)
    def userId = column[String]("_USER_ID")
    def weekId = column[String]("_WEEK_ID")
    def lockedTeam = column[String]("_LOCKED_TEAM")
    def points = column[Double]("_POINTS")
    def created = column[Long]("_CREATED")
    def updated = column[Long]("_UPDATED")

    def * = (id, userId, weekId, lockedTeam, points, created, updated) <> (SlickGame.tupled, SlickGame.unapply)
  }

  val table = TableQuery[Game]

  override def createGame(userId: String, weekId: String, lockedTeamId: String, points: Double): Unit = {
    db.run(table += SlickGame(UUID.randomUUID().toString, userId, weekId, lockedTeamId, points, 1l, 2L))
  }
}
