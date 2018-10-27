package org.zardina.repository

import javax.sql.DataSource
import slick.jdbc.JdbcProfile
import slick.util.AsyncExecutor

import scala.concurrent.{ ExecutionContext, Future }

case class SlickTeam(
  id: String,
  name: String,
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
    def numOfWins: Rep[Int] = column[Int]("_NUM_OF_WINS")
    def numOfLoses: Rep[Int] = column[Int]("_NUM_OF_LOSES")
    def numOfDraws: Rep[Int] = column[Int]("_NUM_OF_DRAWS")
    def created: Rep[Long] = column[Long]("_CREATED")
    def updated: Rep[Long] = column[Long]("_UPDATED")

    def * = (id, name, numOfWins, numOfLoses, numOfDraws, created, updated) <> (SlickTeam.tupled, SlickTeam.unapply)
  }

  private val table = TableQuery[Team]

  override def createTeam(id: String, name: String): Future[Int] = {
    db.run(table += SlickTeam(id, name, 0, 0, 0, 1L, 1L)).map { x => x }
  }
}
