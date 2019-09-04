package org.zardina.repository

import java.util.UUID

import javax.sql.DataSource
import org.zardina.{ Team, repository }
import slick.jdbc.JdbcProfile
import slick.util.AsyncExecutor

import scala.concurrent.{ ExecutionContext, Future }

case class SlickTeam(
  id: String,
  name: String,
  acronym: String,
  numOfWins: Int,
  numOfLoses: Int,
  numOfDraws: Int,
  created: Long,
  updated: Long)

class SlickTeamRepository(dataSource: DataSource)(implicit val profile: JdbcProfile, executionContext: ExecutionContext)
  extends TeamRepository {

  val db: profile.backend.DatabaseDef = profile.backend.Database.forDataSource(dataSource, None, AsyncExecutor.default("locks-team-repository"))
  import profile.api._

  class Team(tag: Tag) extends Table[SlickTeam](tag, "_TEAM") {
    def id: Rep[String] = column[String]("_ID", O.PrimaryKey)
    def name: Rep[String] = column[String]("_NAME")
    def acronym: Rep[String] = column[String]("_ACRONYM")
    def numOfWins: Rep[Int] = column[Int]("_NUM_OF_WINS")
    def numOfLoses: Rep[Int] = column[Int]("_NUM_OF_LOSES")
    def numOfDraws: Rep[Int] = column[Int]("_NUM_OF_DRAWS")
    def created: Rep[Long] = column[Long]("_CREATED")
    def updated: Rep[Long] = column[Long]("_UPDATED")

    def * = (id, name, acronym, numOfWins, numOfLoses, numOfDraws, created, updated) <> (SlickTeam.tupled, SlickTeam.unapply)
  }

  private val table = TableQuery[Team]

  override def createTeam(name: String, acronym: String): Future[Int] = {
    db.run(table += SlickTeam(UUID.randomUUID().toString, name, acronym, 0, 0, 0, 1L, 1L)).map { x => x }
  }

  def team(acronym: String) = {
    db.run(table.filter(_.acronym === acronym).result.headOption).flatMap {
      case Some(t) => Future.successful(Team(t.name, t.acronym, t.numOfWins, t.numOfLoses, t.numOfDraws, 1L, 1L))
      case None => Future.failed(new IllegalArgumentException("failed to find user with specified email"))
    }
  }
}

object AllTeams {
  val teams = Seq(
    ("Arizona Cardinals", "ARI"),
    ("Chicago Bears", "CHI"),
    ("Green Bay Packers", "GB"),
    ("New York Giants", "NYG"),
    ("Detroit Lions", "DET"),
    ("Washington Redskins", "WAS"),
    ("Philadelphia Eagles", "PHI"),
    ("Pittsburgh Steelers", "PIT"),
    ("Los Angeles Rams", "LA"),
    ("San Francisco 49ers", "SF"),
    ("Cleveland Browns", "CLE"),
    ("Indianapolis Colts", "IND"),
    ("Dallas Cowboys", "DAL"),
    ("Kansas City Chiefs", "KC"),
    ("Los Angeles Chargers", "LAC"),
    ("Denver Broncos", "DEN"),
    ("New York Jets", "NYJ"),
    ("New England Patriots", "NE"),
    ("Oakland Raiders", "OAK"),
    ("Tennessee Titans", "TEN"),
    ("Buffalo Bills", "BUF"),
    ("Minnesota Vikings", "MIN"),
    ("Atlanta Falcons", "ATL"),
    ("Miami Dolphins", "MIA"),
    ("New Orleans Saints", "NO"),
    ("Cincinnati Bengals", "CIN"),
    ("Seattle Seahawks", "SEA"),
    ("Tampa Bay Buccaneers", "TB"),
    ("Carolina Panthers", "CAR"),
    ("Jacksonville Jaguars", "JAX"),
    ("Baltimore Ravens", "BAL"),
    ("Houston Texans", "HOU"))
}
