package org.zardina.repository

import java.util.UUID

import javax.sql.DataSource
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

case class SlickLock(id: String, userId: String, weekId: String, lockedTeam: String, points: Double, created: Long, updated: Long)

class SlickLockRepository(dataSource: DataSource)(implicit val jdbcProfile: JdbcProfile, executionContext: ExecutionContext) extends LockRepository {

  import jdbcProfile.api._

  val db = jdbcProfile.backend.Database.forDataSource(dataSource, None, AsyncExecutor.default("locks-game-repository"))

  class Lock(tag: Tag) extends Table[SlickLock](tag, "_LOCK") {
    def id = column[String]("_ID", O.PrimaryKey)
    def userId = column[String]("_USER_ID")
    def weekId = column[String]("_WEEK_ID")
    def lockedTeam = column[String]("_LOCKED_TEAM")
    def points = column[Double]("_POINTS")
    def created = column[Long]("_CREATED")
    def updated = column[Long]("_UPDATED")

    def * = (id, userId, weekId, lockedTeam, points, created, updated) <> (SlickLock.tupled, SlickLock.unapply)
  }

  val table = TableQuery[Lock]

  override def createLock(userId: String, weekId: String, lockedTeamId: String, points: Double): Unit = {
    db.run(table += SlickLock(UUID.randomUUID().toString, userId, weekId, lockedTeamId, points, 1l, 2L))
  }
}
