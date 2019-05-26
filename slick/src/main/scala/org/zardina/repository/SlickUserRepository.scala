package org.zardina.repository
import java.util.UUID

import javax.sql.DataSource
import org.zardina
import org.zardina.User
import slick.jdbc.JdbcProfile
import slick.util.AsyncExecutor
import slick.lifted.{ AbstractTable, CanBeQueryCondition, Query }

import scala.concurrent.{ ExecutionContext, Future }

case class SlickUser(
  id: String,
  nick: String,
  email: String,
  created: Long,
  updated: Long)

class SlickUserRepository(dataSource: DataSource)(implicit val profile: JdbcProfile, executionContext: ExecutionContext)
  extends UserRepository {

  val db: profile.backend.DatabaseDef = profile.backend.Database.forDataSource(dataSource, None, AsyncExecutor.default("locks-user-repository"))
  import profile.api._

  class User(tag: Tag) extends Table[SlickUser](tag, "_USER") {
    def id: Rep[String] = column[String]("_ID", O.PrimaryKey)
    def nick: Rep[String] = column[String]("_NICK", O.PrimaryKey)
    def email: Rep[String] = column[String]("_EMAIL", O.PrimaryKey)
    def created: Rep[Long] = column[Long]("_CREATED")
    def updated: Rep[Long] = column[Long]("_UPDATED")

    def * = (id, nick, email, created, updated) <> (SlickUser.tupled, SlickUser.unapply)
  }

  private val userTable = TableQuery[User]

  override def createUser(nick: String, email: String): Future[org.zardina.User] = {
    db.run(userTable += SlickUser(UUID.randomUUID().toString, nick, email, 1L, 1L))
      .map { x => org.zardina.User(email, nick) }
  }

  override def getUser(email: String): Future[Option[zardina.User]] = Future.successful(Some(User("gogo", "gogo_nick")))

}
