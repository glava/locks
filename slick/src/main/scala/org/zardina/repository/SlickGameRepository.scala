package org.zardina.repository

import java.util.UUID

import javax.sql.DataSource
import org.zardina
import slick.jdbc.JdbcProfile
import slick.util.AsyncExecutor

import scala.concurrent.{ ExecutionContext, Future }

case class SlickGame(
  id: String,
  homeTeamId: String,
  visitorTeamId: String,
  weekNumber: Int,
  created: Long,
  updated: Long)

class SlickGameRepository(dataSource: DataSource)(implicit val profile: JdbcProfile, executionContext: ExecutionContext)
  extends GameRepository {

  val db: profile.backend.DatabaseDef = profile.backend.Database.forDataSource(dataSource, None, AsyncExecutor.default("locks-team-repository"))

  import profile.api._

  class Week(tag: Tag) extends Table[SlickGame](tag, "_GAME") {
    def id: Rep[String] = column[String]("_ID")

    def homeTeamId: Rep[String] = column[String]("_HOME_TEAM_ID", O.PrimaryKey)

    def awayTeamId: Rep[String] = column[String]("_VISITOR_TEAM_ID", O.PrimaryKey)

    def weekNumber: Rep[Int] = column[Int]("_WEEK_NUMBER", O.PrimaryKey)

    def visitorTeamPoints: Rep[Option[Double]] = column[Option[Double]]("_VISITOR_TEAM_POINTS")

    def homeTeamPoints: Rep[Option[Double]] = column[Option[Double]]("_HOME_TEAM_POINTS")

    def homeTeamWin: Rep[Option[Boolean]] = column[Option[Boolean]]("_HOME_TEAM_WIN")

    def created: Rep[Long] = column[Long]("_CREATED")

    def updated: Rep[Long] = column[Long]("_UPDATED")

    def * = (id, homeTeamId, awayTeamId, weekNumber, created, updated) <> (SlickGame.tupled, SlickGame.unapply)
  }

  private val table = TableQuery[Week]

  override def createGame(homeTeam: String, visitorTeam: String, weekNumber: Int): Future[Int] = {
    db.run(table += SlickGame(UUID.randomUUID().toString, homeTeam, visitorTeam, weekNumber, 1l, 1l))
  }

  override def getGames(week: String): Future[Seq[zardina.Game]] = {
    db.run(table.filter(_.weekNumber === week.toInt).result).map {
      _.map { p =>
        zardina.Game(p.homeTeamId, p.visitorTeamId, p.weekNumber)
      }
    }
  }
}
