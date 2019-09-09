package org.zardina
import io.circe.generic.auto._
import sangria.macros.derive.GraphQLField
import sangria.marshalling.circe._
import sangria.schema.{ Argument, Schema, StringType }

import scala.concurrent.Future

object SangriaSchema {
  import sangria.macros.derive._
  case class AuthProviderEmail(email: String, password: String)
  case class AuthProviderSignupData(email: AuthProviderEmail)

  implicit val UserType = deriveObjectType[Unit, User](
    ObjectTypeDescription("User of the lock"),
    DocumentField("email", "email of the user"),
    DocumentField("nick", "nick of the user"))

  implicit val GameType = deriveObjectType[Unit, Game](
    ObjectTypeDescription("User of the lock"),
    DocumentField("home", "email of the user"),
    DocumentField("away", "nick of the user"),
    DocumentField("week", "nick of the user"))

  implicit val TeamType = deriveObjectType[Unit, Team](
    ObjectTypeDescription("NFL Team"))

  implicit val LockType = deriveObjectType[Unit, Lock](
    ObjectTypeDescription("This is what we play for"))

  val EmailArg = Argument("email", StringType)
  val IdArg = Argument("id", StringType)

  trait ApiQueryContext {

    @GraphQLField
    def games(week: Int): Future[Seq[org.zardina.Game]]

    @GraphQLField
    def getUser(email: String): Future[Option[org.zardina.User]]

    @GraphQLField
    def team(acronym: String): Future[Team]
  }

  trait ApiMutationContext {
    @GraphQLField
    def addUser(name: String, email: String, password: String): Future[org.zardina.User]

    @GraphQLField
    def loadGames: Future[List[org.zardina.Game]]

    @GraphQLField
    def createLock(gameId: String, homeTeamSelected: Boolean, userId: String): Future[Lock]

  }

  trait ApiContext extends ApiQueryContext with ApiMutationContext

  val MutationType = deriveContextObjectType[ApiContext, ApiMutationContext, Unit](identity)
  val QueryType = deriveContextObjectType[ApiContext, ApiQueryContext, Unit](identity)

  val schema = Schema(QueryType, mutation = Some(MutationType))
}
