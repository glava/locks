package org.zardina.repository

import java.util.UUID

import javax.sql.DataSource
import slick.jdbc.JdbcProfile
import slick.util.AsyncExecutor

import scala.concurrent.{ ExecutionContext, Future }

case class SlickWeek(
  id: String,
  homeTeamId: String,
  awayTeamId: String,
  result: Option[String],
  weekNumber: Int,
  created: Long,
  updated: Long)

class SlickWeekRepository(dataSource: DataSource)(implicit val profile: JdbcProfile, executionContext: ExecutionContext)
  extends WeekRepository {

  val db: profile.backend.DatabaseDef = profile.backend.Database.forDataSource(dataSource, None, AsyncExecutor.default("locks-team-repository"))
  import profile.api._

  class Week(tag: Tag) extends Table[SlickWeek](tag, "_WEEK") {
    def id: Rep[String] = column[String]("_ID", O.PrimaryKey)
    def homeTeamId: Rep[String] = column[String]("_HOME_TEAM_ID")
    def awayTeamId: Rep[String] = column[String]("_AWAY_TEAM_ID")
    def result: Rep[Option[String]] = column[Option[String]]("_RESULT")
    def weekNumber: Rep[Int] = column[Int]("_WEEK_NUMBER")
    def created: Rep[Long] = column[Long]("_CREATED")
    def updated: Rep[Long] = column[Long]("_UPDATED")

    def * = (id, homeTeamId, awayTeamId, result, weekNumber, created, updated) <> (SlickWeek.tupled, SlickWeek.unapply)
  }

  private val table = TableQuery[Week]

  override def createWeek(homeTeam: String, awayTeam: String, weekNumber: Int): Future[Int] = {
    db.run(table += SlickWeek(UUID.randomUUID().toString, homeTeam, awayTeam, None, weekNumber, 1l, 1l))
  }
}
