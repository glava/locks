package org.zardina.repository

import java.util.UUID

import javax.sql.DataSource
import org.zardina
import org.zardina.Team
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

case class SlickLock(
  id: String,
  userId: String,
  gameId: String,
  lockedTeam: String,
  points: Double,
  created: Long,
  updated: Long)

case class SlickGame(
  id: String,
  homeTeamId: String,
  visitorTeamId: String,
  weekNumber: Int,
  created: Long,
  updated: Long)

case class SlickUser(
  id: String,
  nick: String,
  email: String,
  password: String,
  created: Long,
  updated: Long)

case class SlickTeam(
  id: String,
  name: String,
  numOfWins: Int,
  numOfLoses: Int,
  numOfDraws: Int,
  created: Long,
  updated: Long)

class Dao(dataSource: DataSource)(implicit val jdbcProfile: JdbcProfile, executionContext: ExecutionContext)
  extends LockRepository
  with GameRepository
  with UserRepository
  with TeamRepository {

  def createDb = {
    Await.result(Future.sequence(AllTeams.teams.map { case (name, acronym) => createTeam(name, acronym) }), Duration.Inf)
    Await.result(createUser("gogo", "gogo", "gogo"), Duration.Inf)
  }

  import jdbcProfile.api._

  val db = jdbcProfile.backend.Database.forDataSource(dataSource, None, AsyncExecutor.default("locks-game-repository"))

  class GameTable(tag: Tag) extends Table[SlickGame](tag, "_GAME") {
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

  class User(tag: Tag) extends Table[SlickUser](tag, "_USER") {
    def id: Rep[String] = column[String]("_ID", O.PrimaryKey)
    def nick: Rep[String] = column[String]("_NICK", O.PrimaryKey)
    def email: Rep[String] = column[String]("_EMAIL", O.PrimaryKey)
    def password: Rep[String] = column[String]("_PASSWORD")
    def created: Rep[Long] = column[Long]("_CREATED")
    def updated: Rep[Long] = column[Long]("_UPDATED")

    def * = (id, nick, email, password, created, updated) <> (SlickUser.tupled, SlickUser.unapply)
  }

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

  lazy val Games = TableQuery[GameTable]
  lazy val Users = TableQuery[User]
  lazy val Teams = TableQuery[Team]

  class Lock(tag: Tag) extends Table[SlickLock](tag, "_LOCK") {
    def id = column[String]("_ID", O.PrimaryKey)
    def userId = column[String]("_USER_ID")
    def gameId = column[String]("_GAME_ID")
    def lockedTeam = column[String]("_LOCKED_TEAM")
    def points = column[Double]("_POINTS")
    def created = column[Long]("_CREATED")
    def updated = column[Long]("_UPDATED")

    def userIdFK = foreignKey("_USER_ID_FK", userId, Users)(_.id)
    def gameIdFk = foreignKey("_GAME_ID_FK", gameId, Games)(_.id)
    def lockedTeamFK = foreignKey("_LOCKED_TEAM_FK", lockedTeam, Teams)(_.id)

    def * = (id, userId, gameId, lockedTeam, points, created, updated) <> (SlickLock.tupled, SlickLock.unapply)
  }

  lazy val Locks = TableQuery[Lock]

  override def createUser(nick: String, email: String, password: String): Future[org.zardina.User] = {
    val slickUserId = SlickUser(UUID.randomUUID().toString, nick, email, password, 1L, 1L)
    db.run(Users += slickUserId)
      .map { _ => org.zardina.User(slickUserId.id, email, nick) }
  }

  override def getUser(email: String): Future[Option[zardina.User]] = {
    db.run(Users.filter(_.email === email).result.headOption).flatMap {
      case Some(t) => Future.successful(Some(org.zardina.User(t.id, t.email, t.nick)))
      case None => Future.failed(new IllegalArgumentException("failed to find user with specified email"))
    }
  }
  override def createTeam(name: String, acronym: String): Future[Int] = {
    db.run(Teams += SlickTeam(acronym, name, 0, 0, 0, 1L, 1L)).map { x => x }
  }

  def team(acronym: String) = {
    db.run(Teams.filter(_.id === acronym).result.headOption).flatMap {
      case Some(t) => Future.successful(Team(t.name, t.id, t.numOfWins, t.numOfLoses, t.numOfDraws, 1L, 1L))
      case None => Future.failed(new IllegalArgumentException("failed to find user with specified email"))
    }
  }
  override def createGame(homeTeam: String, visitorTeam: String, weekNumber: Int): Future[org.zardina.Game] = {
    val slickGame = SlickGame(UUID.randomUUID().toString, homeTeam, visitorTeam, weekNumber, 1l, 1l)
    db.run(Games += slickGame).map(_ => org.zardina.Game(slickGame.id, slickGame.homeTeamId, slickGame.visitorTeamId, weekNumber))
  }

  override def getGame(home: String, away: String, week: Int): Future[zardina.Game] = {
    db.run(Games.filter(_.homeTeamId === home).filter(_.awayTeamId === away).filter(_.weekNumber === week).result.headOption).flatMap {
      case Some(p) => Future.successful(zardina.Game(p.id, p.homeTeamId, p.visitorTeamId, p.weekNumber))
      case None => Future.failed(new IllegalArgumentException("failed to find user with specified email"))
    }
  }

  override def getGame(id: String): Future[zardina.Game] = {
    db.run(Games.filter(_.id === id).result.headOption).flatMap {
      case Some(p) => Future.successful(zardina.Game(p.id, p.homeTeamId, p.visitorTeamId, p.weekNumber))
      case None => Future.failed(new IllegalArgumentException("failed to find user with specified email"))
    }
  }

  override def getGames(week: String): Future[Seq[zardina.Game]] = {
    db.run(Games.filter(_.weekNumber === week.toInt).result).map {
      _.map { slickGame =>
        zardina.Game(slickGame.id, slickGame.homeTeamId, slickGame.visitorTeamId, slickGame.weekNumber)
      }
    }
  }

  override def createLock(userId: String, gameId: String, lockedTeamId: String, points: Double): Future[org.zardina.Lock] = {
    db.run(Locks += SlickLock(UUID.randomUUID().toString, userId, gameId, lockedTeamId, points, 1l, 2L)).map(_ => org.zardina.Lock(userId, gameId, lockedTeamId, points))
  }

  override def getLocks(userId: String, weekId: String): Future[Seq[zardina.Lock]] = {
    val innerJoin = for {
      (lock, _) <- Locks.filter(_.userId === userId) join Games.filter(_.weekNumber === weekId.toInt) on (_.gameId === _.id)
    } yield lock
    db.run(innerJoin.result.map(_.map { lock => org.zardina.Lock(lock.userId, lock.gameId, lock.lockedTeam, lock.points) }))
  }
}
