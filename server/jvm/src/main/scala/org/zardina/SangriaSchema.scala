package org.zardina
import sangria.marshalling.circe._
import io.circe.generic.auto._
import sangria.macros.derive.{DocumentField, InputObjectTypeName, ObjectTypeDescription, deriveInputObjectType, deriveObjectType}
import sangria.schema.{Argument, Field, InputObjectType, ObjectType, OptionType, Schema, StringType, fields}

object SangriaSchema {

  case class AuthProviderEmail(email: String, password: String)
  case class AuthProviderSignupData(email: AuthProviderEmail)

  implicit val UserType = deriveObjectType[Unit, User](
    ObjectTypeDescription("User of the lock"),
    DocumentField("email", "email of the user"),
    DocumentField("nick", "nick of the user")
  )

  val Email = Argument("email", StringType)

  val QueryType = ObjectType("Query", fields[ApiContext, Unit](
    Field("user", OptionType(UserType),
      description = Some("Returns a user with specific `id`."),
      arguments = Email :: Nil,
      resolve = c ⇒ c.ctx.getUser(c arg Email)),
  ))



  implicit val AuthProviderEmailInputType: InputObjectType[AuthProviderEmail] = deriveInputObjectType[AuthProviderEmail](
    InputObjectTypeName("AUTH_PROVIDER_EMAIL")
  )

  lazy val AuthProviderSignupDataInputType: InputObjectType[AuthProviderSignupData] = deriveInputObjectType[AuthProviderSignupData]()


  val NameArg = Argument("name", StringType)
  val PasswordArg = Argument("password", StringType)
  val AuthProviderArg = Argument("authProvider", AuthProviderSignupDataInputType)

  val Mutation = ObjectType(
    "Mutation",
    fields[ApiContext, Unit](
      Field("createUser",
        UserType,
        arguments = NameArg :: PasswordArg :: Email :: Nil,
        resolve = c => c.ctx.addUser(c.arg(NameArg), c.arg(Email), c.arg(PasswordArg))
      )
    )
  )


  val schema = Schema(QueryType, mutation = Some(Mutation))
}
