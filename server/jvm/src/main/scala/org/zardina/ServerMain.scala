package org.zardina
import sangria.schema._

import sangria.macros.derive._
import com.twitter.finagle.Http
import com.twitter.finagle.http.Response
import com.twitter.util.{ Await, Future }
import io.finch.syntax.{ get, post }
import org.zardina.graphql._
import com.twitter.finagle.http.Status
import io.circe.generic.auto._
import io.circe._, io.circe.generic.semiauto._
import sangria.marshalling.circe._
import scala.concurrent.ExecutionContext
import io.circe._
import io.finch.{ Endpoint, _ }
import org.zardina.repository.{ GameRepository, SlickGameRepository, SlickTeamRepository, SlickUserRepository, SlickWeekRepository, TeamRepository, UserRepository, WeekRepository }
import slick.jdbc.H2Profile
object ServerMain extends App
  with StaticResourceSupport
  with GraphQlRequestDecoders
  with GraphQlEncoders {

  DbMigration.updateDatabase(DataSource.mysqlConnection)

  val apiContext: ApiContext = new ApiContext {
    implicit val profile = H2Profile
    implicit val context = ExecutionContext.global

    override val gameRepository: GameRepository = new SlickGameRepository(DataSource.mysqlConnection)
    override val teamRepository: TeamRepository = new SlickTeamRepository(DataSource.mysqlConnection)
    override val userRepository: UserRepository = new SlickUserRepository(DataSource.mysqlConnection)
    override val weekRepository: WeekRepository = new SlickWeekRepository(DataSource.mysqlConnection)
  }

  val executor = GraphQlQueryExecutor.executor(SangriaSchema.schema, apiContext, maxQueryDepth = 10)

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
