package org.zardina

import io.circe.{ Decoder, Json }
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom.ext.{ Ajax, AjaxException }

import scala.concurrent.{ ExecutionContext, Future }

case class GraphQLResponse[T](data: T)
case class GamesList(games: List[Game], locks: List[Lock])
case class LockResult(createLock: Lock)
case class UserResult(user: User)

class LocksApiClient(implicit ex: ExecutionContext) {

  def query[T](query: String, variables: Map[String, Json] = Map.empty)(implicit ev: Decoder[T]): Future[GraphQLResponse[T]] = {
    val queryJson = Json.obj(
      "query" -> Json.fromString(query),
      "variables" -> Json.fromFields(variables)).noSpaces
    println(queryJson)
    Ajax.post("/api", queryJson, headers = Map("Content-Type" -> "application/json")).flatMap(response => decode[GraphQLResponse[T]](response.responseText) match {
      case Right(parsed) =>
        println(parsed)
        Future.successful(parsed)
      case Left(error) =>
        println(s"error while process api query: ${error.getMessage}, ${response.responseText}, ${response.status}, ${response.statusText}")
        error.printStackTrace()
        Future.failed(error)
    }).transform(identity[GraphQLResponse[T]], error => error match {
      case ajax: AjaxException =>
        println(ajax)
        val errorResponse: Json = decode[Json](ajax.xhr.responseText).toOption.flatMap(_.asObject).get("error").getOrElse(Json.fromString(error.getMessage))
        println(errorResponse)
        new RuntimeException(errorResponse.asString.getOrElse(""))
      case error: Throwable => new RuntimeException(error.getMessage)
    })
  }

  def createLock(gameId: String, homeTeamSelected: Boolean, userId: String): Future[LockResult] =
    query[LockResult](
      s"""
         |mutation {
         |createLock(gameId: "${gameId}", homeTeamSelected: ${homeTeamSelected}, userId:"${userId}") {
         |userId, gameId, lockedTeam, points
         |}
         |}
         |
       """.stripMargin).map(_.data)

  def user(email: String): Future[UserResult] =
    query[UserResult](
      s"""
         |query {
         |   user(email:"gogo")
         |  { id, email, nick }
         |}
         |
       """.stripMargin).map(_.data)

  def games(week: Int, userId: String): Future[GamesList] =
    query[GamesList](
      s"""
         |query {
         |  games (week: ${week})
         |  { id, home, away, week  },
         |  locks(userId:"${userId}", week: ${week})
         |  { userId, gameId, lockedTeam, points }
         |}
         |
       """.stripMargin).map(_.data)

  def selection(): Future[Boolean] = {
    Ajax.post("/selection").flatMap(response => decode[Boolean](response.responseText) match {
      case Left(r) =>
        println(r)
        Future.failed(r)
      case Right(r) =>
        println(r)
        Future.successful(r)
    })
  }
}
