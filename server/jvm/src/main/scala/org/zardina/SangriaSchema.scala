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

  implicit val WeekType = deriveObjectType[Unit, Week](
    ObjectTypeDescription("User of the lock"),
    DocumentField("home", "email of the user"),
    DocumentField("away", "nick of the user"),
    DocumentField("week", "nick of the user"))

  val EmailArg = Argument("email", StringType)
  val IdArg = Argument("id", StringType)

  trait ApiQueryContext {

    @GraphQLField
    def getGames(week: Int): Future[Seq[org.zardina.Week]]

    @GraphQLField
    def getUser(email: String): Future[Option[org.zardina.User]]
  }

  trait ApiMutationContext {
    @GraphQLField
    def addUser(name: String, email: String, password: String): Future[org.zardina.User]

    @GraphQLField
    def updateGames: Future[List[Int]]
  }

  trait ApiContext extends ApiQueryContext with ApiMutationContext

  val MutationType = deriveContextObjectType[ApiContext, ApiMutationContext, Unit](identity)
  val QueryType = deriveContextObjectType[ApiContext, ApiQueryContext, Unit](identity)

  val schema = Schema(QueryType, mutation = Some(MutationType))
}
