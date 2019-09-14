package org.zardina

import com.twitter.finagle.Http
import com.twitter.finagle.http.Response
import io.finch.syntax.{ get, post }
import org.zardina.graphql._
import com.twitter.finagle.http.Status
import com.twitter.util.Await

import scala.concurrent.{ ExecutionContext, Future, Await => AWW }
import io.circe._
import io.finch.{ Endpoint, _ }
import org.zardina.authentication.AuthMiddleware
import org.zardina.repository._
import sangria.execution.{ HandledException, Middleware, MiddlewareBeforeField, MiddlewareQueryContext }
import sangria.execution.{ ExceptionHandler => EHandler, _ }
import sangria.schema.Context
import slick.jdbc.H2Profile

import scala.concurrent.duration.Duration

object ServerMain extends App
  with StaticResourceSupport
  with GraphQlRequestDecoders
  with GraphQlEncoders {
  implicit val profile = H2Profile
  implicit val context = ExecutionContext.global

  DbMigration.updateDatabase(DataSource.h2Connection)

  lazy val apiContext: ApiContextImplementation = new ApiContextImplementation {
    implicit val profile = H2Profile
    implicit val context = ExecutionContext.global

    override val dao = new Dao(DataSource.h2Connection)

    dao.createDb
    // load games
    AWW.result(
      Future.sequence(staticGameDetails.map {
        g => dao.createGame(g.homeTeamAbbr, g.visitorTeamAbbr, g.week)
      }),
      Duration.Inf)
  }

  val ErrorHandler = EHandler {
    case (_, AuthenticationException(message)) ⇒ HandledException(message)
    case (_, AuthorizationException(message)) ⇒ HandledException(message)
  }

  val executor = GraphQlQueryExecutor.executor(
    SangriaSchema.schema,
    apiContext,
    10,
    ErrorHandler,
    AuthMiddleware)

  val index: Endpoint[Response] = get("index") {
    getResource("index.html", "text/html")
  }

  val api: Endpoint[Json] =
    post("api" :: jsonBody[GraphQlQuery]) { query: GraphQlQuery =>
      val result = executor.execute(query)(ExecutionContext.global)

      // Do our best to map the type of error back to a HTTP status code
      result.map {
        case SuccessfulGraphQlResult(json, _) => Output.payload(json, Status.Ok)
        case ClientErrorGraphQlResult(json, _, _) => Output.payload(json, Status.BadRequest)
        case BackendErrorGraphQlResult(json, _, _) => Output.payload(json, Status.InternalServerError)
      }
    }

  def httpGetResource(resource: String, contentType: String): Endpoint[Response] = get(resource) {
    getResource(resource, contentType)
  }

  val resources = {
    httpGetResource("jquery.min.js", "application/javascript") :+:
      httpGetResource("react.min.js", "application/javascript") :+:
      httpGetResource("fetch.min.js", "application/javascript") :+:
      httpGetResource("react-dom.min.js", "application/javascript") :+:
      httpGetResource("es6-promise.auto.min.js", "application/javascript") :+:
      httpGetResource("bootstrap.min.js", "application/javascript") :+:
      httpGetResource("toastr.min.js", "application/javascript") :+:
      httpGetResource("ace.js", "application/javascript") :+:
      httpGetResource("theme-textmate.js", "application/javascript") :+:
      httpGetResource("mode-json.js", "application/javascript") :+:
      httpGetResource("worker-json.js", "application/javascript") :+:
      httpGetResource("locks-ui.js", "application/javascript") :+:
      httpGetResource("locks-ui.js.map", "application/json") :+:
      httpGetResource("toastr.min.css", "text/css") :+:
      httpGetResource("bootstrap.min.css", "text/css") :+:
      httpGetResource("bootswatch.lumen.min.css", "text/css") :+:
      httpGetResource("fontawesome-all.css", "text/css") :+:
      httpGetResource("style.css", "text/css") :+:
      httpGetResource("style.css", "text/css") :+:
      httpGetResource("locks.css", "text/css") :+:
      httpGetResource("graphiql.html", "text/html") :+:
      httpGetResource("index.html", "text/html") :+:
      httpGetResource("graphiql.min.css", "text/css") :+:
      httpGetResource("graphiql.min.js", "application/javascript")
  }

  try {
    Await.ready(Http.server.serve(":8081", (index :+: resources :+: api).toService))
  } catch {
    case e: Exception => println(e.getLocalizedMessage)
  }
}
